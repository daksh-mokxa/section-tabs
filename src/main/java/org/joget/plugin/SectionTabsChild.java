package org.joget.plugin;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.lib.SelectBox;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.Section;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.StringUtil;
import org.joget.plugin.base.PluginWebSupport;


public class SectionTabsChild extends Section implements PluginWebSupport{
    protected Element wrappedSection;

    public void setWrappedSection(Element section) {
        this.wrappedSection = section;
    }

    public Element getWrappedSection() {
        return wrappedSection;
    }

    protected Element getContentRoot() {
        return wrappedSection != null ? wrappedSection : this;
    }

    @Override
    public Collection<Element> getChildren() {
        if (wrappedSection != null) {
            return wrappedSection.getChildren();
        }
        return super.getChildren();
    }

    @Override
    public Collection<Element> getChildren(FormData formData) {
        if (wrappedSection != null) {
            return wrappedSection.getChildren(formData);
        }
        return super.getChildren(formData);
    }

    @Override
    public String getName() {
        return "SectionTabsChild";
    }
    
    @Override
    public String getVersion() {
        return Activator.VERSION;
    }
    
    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        if (isLoad(formData)) {
            setProperty("load", "true");
        }
        
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        dataModel.put("appId", appDef.getAppId());
        dataModel.put("appVersion", appDef.getVersion());
        
        Form form = FormUtil.findRootForm(this);
        dataModel.put("formDefId", form.getPropertyString(FormUtil.PROPERTY_ID));
        dataModel.put("formTableName", form.getPropertyString(FormUtil.PROPERTY_TABLE_NAME));
        
        dataModel.put("primaryKey", (formData.getPrimaryKeyValue() != null)?formData.getPrimaryKeyValue():"");
        dataModel.put("processId", (formData.getProcessId() != null)?formData.getProcessId():"");
        dataModel.put("activityId", (formData.getActivityId() != null)?formData.getActivityId():"");
        ElementRepairUtil.repairElementTree(getContentRoot());
        return super.renderTemplate(formData, dataModel);
    }

    public String renderChild(Element child, FormData formData, boolean includeMetaData) {
        if (child == null) {
            return "";
        }
        try {
            ElementRepairUtil.repairElementTree(child);
            return child.render(formData, includeMetaData);
        } catch (Throwable e) {
            return "";
        }
    }

    public boolean isLoad(FormData formData) {
        String paramName = FormUtil.getElementParameterName(this);
        if ((FormUtil.isFormSubmitted(this, formData) && formData.getRequestParameter(paramName + "_loaded") != null) || formData.getFormResult("FORM_RESULT_LOAD_ALL_DATA") != null) {
            return true;
        }
        return false;
    }
    
    public String getJson() {
        try {
            Element jsonRoot = getContentRoot();
            if (getLoadBinder() == null) {
                Form form = FormUtil.findRootForm(this);
                setLoadBinder(form.getLoadBinder());
            }
            recursiveDisableSectionTab(jsonRoot);
            return FormUtil.generateElementJson(jsonRoot);
        } catch (Exception e) {
        }
        return "";
    }
    
    protected void recursiveDisableSectionTab(Element e) {
        Collection<Element> childs = e.getChildren();
        if (!childs.isEmpty()) {
            for (Element c : childs) {
                if (c instanceof SectionTabs) {
                    c.setProperty("__DISABLED__", "true");
                }
                recursiveDisableSectionTab(c);
            }
        }
    }
    
    protected void recursiveEnableSectionTab(Element e) {
        Collection<Element> childs = e.getChildren();
        if (!childs.isEmpty()) {
            for (Element c : childs) {
                if (c instanceof SectionTabs) {
                    c.setProperty("__DISABLED__", "false");
                }
                recursiveEnableSectionTab(c);
            }
        }
    }
    
    public String getNonce() {
        String nonce = SecurityUtil.generateNonce(new String[]{"SectionTabsChild", getPropertyString("id")}, 2);
        return nonce;
    }
    
    @Override
    public FormData formatDataForValidation(FormData formData) {
        formData = addSelectFieldValuesToRequest(this, formData);
        return formData;
    }
    
    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        FormData formData = new FormData();
        formData.setPrimaryKeyValue(StringUtil.escapeString(request.getParameter("id"), StringUtil.TYPE_HTML, null));
        formData.setProcessId(StringUtil.escapeString(request.getParameter("_processId"), StringUtil.TYPE_HTML, null));
        formData.setActivityId(StringUtil.escapeString(request.getParameter("activityId"), StringUtil.TYPE_HTML, null));
        
        String nonce = request.getParameter("_nonce");
        if (!SecurityUtil.verifyNonce(nonce, new String[]{"SectionTabsChild", request.getParameter("_elementId")})) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        //get app def to set CurrentAppDefinition
        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
        AppDefinition appDef = appService.getAppDefinition(request.getParameter("appId"), request.getParameter("appVersion"));

        //get new html
        String content = "";
        try {
            String json = request.getParameter("_element_json");
            Form form = new Form();
            form.setProperty(FormUtil.PROPERTY_ID, StringUtil.escapeString(request.getParameter("_formDefId"), StringUtil.TYPE_HTML, null));
            form.setProperty(FormUtil.PROPERTY_TABLE_NAME, StringUtil.escapeString(request.getParameter("_formTableName"), StringUtil.TYPE_HTML, null));
            Element parsed = FormUtil.parseElementFromJson(json);
            ElementRepairUtil.repairElementTree(parsed);
            SectionTabsChild section = new SectionTabsChild();
            section.setWrappedSection(parsed);
            section.setProperties(parsed.getProperties());
            section.setParent(form);

            recursiveEnableSectionTab(parsed);
            Collection<Element> child = new ArrayList<Element>();
            child.add(section);
            form.setChildren(child);
            FormUtil.executeOptionBinders(form, formData);
            FormUtil.executeLoadBinders(form, formData);

            for (Element e : section.getChildren(formData)) {
                content += section.renderChild(e, formData, false);
            }
        } catch (Exception e) {
        }

        if (content != null && !content.isEmpty()) {
            //return html
            response.setContentType("text/html");
            PrintWriter writer = response.getWriter();
            writer.write(content);
        } else {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }
    
    protected FormData addSelectFieldValuesToRequest(Element element, FormData formData) {
        if (element instanceof SelectBox) {
            String paramName = FormUtil.getElementParameterName(element);
            String secName = FormUtil.getElementParameterName(this);
            String[] loaded = formData.getRequestParameterValues(secName + "_loaded");

            if (formData.getRequestParameter(paramName) == null && FormUtil.isFormSubmitted(element, formData)) {
                String[] paramValues = FormUtil.getElementPropertyValues(element, formData);
                if (loaded != null) {
                    if ("true".equals(loaded[0])) {
                        formData.addRequestParameterValues(paramName, new String[]{""});
                    } else {
                        formData.addRequestParameterValues(paramName, paramValues);
                    }
                }
            }
        }
        
        // recurse into children
        Collection<Element> children = element.getChildren(formData);
        if (children != null) {
            for (Element child : children) {
                formData = addSelectFieldValuesToRequest(child, formData);
            }
        }
        return formData;
    }
}
