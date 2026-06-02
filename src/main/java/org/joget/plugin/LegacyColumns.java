package org.joget.plugin;

import java.util.Collection;
import java.util.Map;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormContainer;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.service.FormUtil;

/**
 * Compatibility renderer for legacy form definitions that reference
 * org.joget.apps.form.lib.Columns (not available in Joget DX 9.0.x).
 */
public class LegacyColumns extends Element implements FormContainer {

    @Override
    public String getName() {
        return "Columns (Legacy)";
    }

    @Override
    public String getVersion() {
        return Activator.VERSION;
    }

    @Override
    public String getDescription() {
        return "Legacy multi-column layout";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getLabel() {
        return "Columns";
    }

    @Override
    public String getPropertyOptions() {
        return "";
    }

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String template = "legacyColumns.ftl";
        return FormUtil.generateElementHtml(this, formData, template, dataModel);
    }

    public String renderChild(Element child, FormData formData, boolean includeMetaData) {
        return ElementRepairUtil.safeRender(child, formData, includeMetaData);
    }
}
