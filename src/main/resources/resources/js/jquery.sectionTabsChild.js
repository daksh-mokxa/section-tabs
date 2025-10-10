(function($){
    $.fn.extend({
        sectionTabChild : function(o){
            var target = this;
            if($(target)){
                $(target).on('section_wizard_step_shown', function(){
                    if ($(target).find('.temp_content').length > 0) {
                        loadContent(target, o);
                    }
                });
            }
            return target;
        }
    });
    
    function loadContent(target, o){
        var data = {
                id : o.primaryKey,
                _elementId : o.id,
                _processId : o.processId,
                activityId : o.activityId,
                appId : o.appId,
                appVersion : o.appVersion,
                _formDefId : o.formDefId,
                _formTableName : o.formTableName,
                _nonce : o.nonce,
                _element_json : $(target).find(".temp_content .child_json").val()
            };
        
        //pass request parameter to ajax call
        var sPageURL = decodeURIComponent(window.location.search.substring(1)),
        sURLVariables = sPageURL.split('&'),
        sParameterName,
        i;

        for (i = 0; i < sURLVariables.length; i++) {
            sParameterName = sURLVariables[i].split('=');
            const key = sParameterName[0];
            const value = sParameterName[1];

            if (key && key.trim() !== "") {
                data[key] = (value === undefined ? true : value);
            }
        }
        
        $.ajax({
            type: "POST",
            data: data,
            dataType : "text",
            url: o.contextPath + '/web/json/plugin/org.joget.plugin.SectionTabsChild/service',
            success: function(response) {
                $(target).find('> .temp_content').after(response);
                $(target).find('> .temp_content').after('<input type="hidden" name="'+o.paramName+'_loaded" value="true" />');
                $(target).find('> .temp_content').remove();
            }
        });
    }
})(jQuery);