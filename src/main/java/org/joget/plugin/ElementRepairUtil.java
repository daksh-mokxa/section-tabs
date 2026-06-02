package org.joget.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.joget.apps.form.model.Column;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.service.FormUtil;

final class ElementRepairUtil {

    private static final String LEGACY_COLUMNS = "org.joget.apps.form.lib.Columns";
    private static final String LEGACY_COLUMN_CONTAINER = "org.joget.apps.form.lib.ColumnContainer";

    private ElementRepairUtil() {
    }

    static void repairElementTree(Element root) {
        if (root != null) {
            repairChildren(root);
        }
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

    private static Element tryRepairElement(Element element) {
        if (!isMissingElement(element)) {
            return element;
        }
        String configuredClass = configuredClassName(element);
        if (LEGACY_COLUMN_CONTAINER.equals(configuredClass)) {
            return repairViaJsonString(element);
        }
        if (LEGACY_COLUMNS.equals(configuredClass)) {
            return repairViaJsonString(element);
        }
        return element;
    }

    private static Element repairViaJsonString(Element missing) {
        try {
            String json = FormUtil.generateElementJson(missing);
            if (json == null || json.isEmpty()) {
                return missing;
            }

            json = json.replace(LEGACY_COLUMNS, LegacyColumns.class.getName());
            json = json.replace(LEGACY_COLUMN_CONTAINER, Column.class.getName());

            json = json.replace("\"columns\":", "\"elements\":");

            Element repaired = FormUtil.parseElementFromJson(json);
            if (repaired != null) {
                copyElementIdentity(missing, repaired);
                return repaired;
            }
        } catch (Exception e) {
        }
        return missing;
    }

    private static Collection<Element> repairChildCollection(Collection<Element> children) {
        List<Element> repaired = new ArrayList<Element>();
        if (children == null) {
            return repaired;
        }
        for (Element child : children) {
            Element fixed = tryRepairElement(child);
            if (isMissingElement(fixed) && LEGACY_COLUMN_CONTAINER.equals(configuredClassName(fixed))) {
                fixed = repairViaJsonString(fixed);
            }
            repaired.add(fixed);
        }
        return repaired;
    }

    private static void replaceChild(Element parent, Element oldChild, Element newChild) {
        Collection<Element> children = parent.getChildren();
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

    private static void copyElementIdentity(Element source, Element target) {
        String customParameterName = source.getCustomParameterName();
        if (customParameterName != null && !customParameterName.isEmpty()) {
            target.setCustomParameterName(customParameterName);
        }
        String customId = source.getPropertyString("customId");
        if (customId != null && !customId.isEmpty()) {
            target.setProperty("customId", customId);
        }
    }

    private static boolean isMissingElement(Element element) {
        return element.getClass().getName().contains("MissingElement");
    }

    private static String configuredClassName(Element element) {
        String className = element.getClassName();
        if (className == null || className.isEmpty()) {
            className = element.getPropertyString("className");
        }
        return className;
    }
}
