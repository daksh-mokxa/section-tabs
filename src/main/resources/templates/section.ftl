<div id="${elementParamName!}" class="form-section step <#if element.hasError(formData) >error</#if> <#if !(element.properties.label?? && element.properties.label != "") >no_label</#if> section_${element.properties.elementUniqueKey!}" ${elementMetaData!} <#if visible == false && includeMetaData == false>style="display: none"</#if>>
    <div class="form-section-title"><#if element.properties.label?? && element.properties.label != ""><span>${element.properties.label!}</span></#if></div>

    <#if (element.properties.load?? && element.properties.load == "true")>
    <input type="hidden" name="${elementParamName!}_loaded" value="true" />
        <#list element.children as e>
            ${e.render(formData, includeMetaData!false)}
        </#list>
    <#else>
    <div class="temp_content"> 
        <input class="child_json" type="hidden" readonly="true" name="${elementParamName!}_loaded" value="${element.json?html}" />
        <img src="${request.contextPath}/images/v3/loading.gif" />
        </div>
    </#if>
<#if !(request.getAttribute("org.joget.plugin.SectionTabsChild")??) >
    <script type="text/javascript" src="${request.contextPath}/plugin/org.joget.plugin.SectionTabsChild/js/jquery.sectionTabsChild.js"></script>
</#if>     
<#if rules?? && includeMetaData == false>
    <script type="text/javascript">
        $(document).ready(function() {
            new VisibilityMonitor($('.section_${element.properties.elementUniqueKey!}'), ${rules}).init();
        });
        </script>
</#if>
<#if includeMetaData == false>
    <script type="text/javascript">
        $('#${elementParamName!}').sectionTabChild({
            contextPath: "${request.contextPath}",
            id: "${element.properties.id!}",
            paramName : '${elementParamName!}',
            primaryKey : '${primaryKey?html}',
            processId : '${processId?html}',
            activityId : '${activityId?html}',
            formDefId : '${formDefId?html}',
            formTableName : '${formTableName?html}',
            nonce : '${element.nonce?html}',
            appId : "${appId!}",
            appVersion : "${appVersion!}"
        });
        </script>
    <div style="clear:both"></div>
</#if>
    </div>
