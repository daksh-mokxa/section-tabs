<div id="${elementParamName!}" class="form-columns legacy-columns<#if element.properties['css-mobile-stack-columns']?? && element.properties['css-mobile-stack-columns'] == 'true'> form-columns-mobile-stack</#if>" style="<#if element.properties.gutter??>gap: ${element.properties.gutter};</#if>" ${elementMetaData!}>
    <#list element.children as e>
        ${element.renderChild(e, formData, includeMetaData!false)}
    </#list>
</div>
