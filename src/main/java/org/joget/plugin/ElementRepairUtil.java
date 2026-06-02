package org.joget.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.joget.apps.form.model.Column;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;

final class ElementRepairUtil {

    private static final String LEGACY_COLUMNS = "org.joget.apps.form.lib.Columns";
    private static final String LEGACY_COLUMN_CONTAINER = "org.joget.apps.form.lib.ColumnContainer";

    private ElementRepairUtil() {
    }

    static void repairElementTree(Element root) {
        if (root == null) {
            return;
        }
        repairChildren(root);
    }

    private static void repairChildren(Element parent) {
        Collection<Element> children = parent.getChildren();
        if (children == null || children.isEmpty()) {
            return;
        }
        List<Element> snapshot = new ArrayList<Element>(children);
        for (Element child : snapshot) {
            repairChildren(child);
            Element repaired = tryRepairElement(child);
            if (repaired != child) {
                replaceChild(parent, child, repaired);
                repairChildren(repaired);
            }
        }
    }

    static Element tryRepairElement(Element element) {
        if (element == null || !isMissingElement(element)) {
            return element;
        }
        String configuredClass = configuredClassName(element);
        try {
            if (LEGACY_COLUMN_CONTAINER.equals(configuredClass)) {
                return repairAsColumn(element);
            }
            if (LEGACY_COLUMNS.equals(configuredClass)) {
                return repairAsLegacyColumns(element);
            }
        } catch (Exception e) {
            LogUtil.warn(LegacyColumns.class.getName(), "Could not repair legacy element id="
                    + element.getPropertyString(FormUtil.PROPERTY_ID) + " class=" + configuredClass
                    + ": " + e.getMessage());
        }
        return element;
    }

    static Element repairAsColumn(Element source) {
        Column column = new Column();
        column.setProperties(source.getProperties());
        copyElementIdentity(source, column);
        column.setChildren(repairChildCollection(source.getChildren()));
        return column;
    }

    static Element repairAsLegacyColumns(Element source) {
        LegacyColumns columns = new LegacyColumns();
        columns.setProperties(source.getProperties());
        copyElementIdentity(source, columns);
        columns.setChildren(repairChildCollection(source.getChildren()));
        return columns;
    }

    private static Collection<Element> repairChildCollection(Collection<Element> children) {
        List<Element> repaired = new ArrayList<Element>();
        if (children == null) {
            return repaired;
        }
        for (Element child : children) {
            Element fixed = tryRepairElement(child);
            if (isMissingElement(fixed) && LEGACY_COLUMN_CONTAINER.equals(configuredClassName(fixed))) {
                fixed = repairAsColumn(fixed);
            }
            repaired.add(fixed);
        }
        return repaired;
    }

    static void replaceChild(Element parent, Element oldChild, Element newChild) {
        Collection<Element> children = parent.getChildren();
        if (children == null) {
            return;
        }
        if (children instanceof List) {
            List<Element> list = (List<Element>) children;
            int index = list.indexOf(oldChild);
            if (index >= 0) {
                newChild.setParent(parent);
                list.set(index, newChild);
                return;
            }
        }
        List<Element> updated = new ArrayList<Element>();
        for (Element child : children) {
            updated.add(child == oldChild ? newChild : child);
        }
        parent.setChildren(updated);
    }

    static void copyElementIdentity(Element source, Element target) {
        String customParameterName = source.getCustomParameterName();
        if (customParameterName != null && !customParameterName.isEmpty()) {
            target.setCustomParameterName(customParameterName);
        }
        String customId = source.getPropertyString("customId");
        if (customId != null && !customId.isEmpty()) {
            target.setProperty("customId", customId);
        }
    }

    static String safeRender(Element element, FormData formData, boolean includeMetaData) {
        if (element == null) {
            return "";
        }
        if (isMissingElement(element)) {
            Element repaired = tryRepairElement(element);
            if (repaired != element) {
                try {
                    return repaired.render(formData, includeMetaData);
                } catch (Throwable e) {
                    LogUtil.error(LegacyColumns.class.getName(), e, "Render failed after repair for id="
                            + repaired.getPropertyString(FormUtil.PROPERTY_ID));
                }
            }
            return missingElementHtml(element);
        }
        try {
            return element.render(formData, includeMetaData);
        } catch (Throwable e) {
            LogUtil.error(LegacyColumns.class.getName(), e, "Render failed for id="
                    + element.getPropertyString(FormUtil.PROPERTY_ID) + " class=" + element.getClassName());
            return "<div class=\"form-cell form-error\"><span class=\"form-error-message\">Unable to render field "
                    + element.getPropertyString(FormUtil.PROPERTY_ID) + "</span></div>";
        }
    }

    static boolean isMissingElement(Element element) {
        return element.getClass().getName().contains("MissingElement");
    }

    private static String configuredClassName(Element element) {
        String className = element.getClassName();
        if (className == null || className.isEmpty()) {
            className = element.getPropertyString("className");
        }
        return className;
    }

    private static String missingElementHtml(Element element) {
        String configuredClass = configuredClassName(element);
        return "<div class=\"form-cell form-error\"><span class=\"form-error-message\">Missing form element ("
                + configuredClass + " / " + element.getPropertyString(FormUtil.PROPERTY_ID)
                + "). Check form definition or installed plugins.</span></div>";
    }
}
