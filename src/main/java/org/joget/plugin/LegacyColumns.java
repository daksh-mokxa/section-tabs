package org.joget.plugin;

import java.util.Map;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormContainer;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.service.FormUtil;

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
        return "";
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
        return FormUtil.generateElementHtml(this, formData, "legacyColumns.ftl", dataModel);
    }
}
