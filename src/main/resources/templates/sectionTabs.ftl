<#if includeMetaData>
    <div class="form-cell" ${elementMetaData!}>
        <span class='form-floating-label'>SECTION TABS</span>
    </div>
<#else>
    <div id="${elementParamName!}" class="tabs">
        <#list element.children as e>
            ${e.render(formData, includeMetaData!false)}
        </#list>
    </div>
    <script>
        $(document).ready(function(){
            $("${selector!}").addClass("step");
            $("${selector!}").appendTo("#${elementParamName!}");
            $("#${elementParamName!}").css("max-width", $("#${elementParamName!}").width() + "px");
            $("#${elementParamName!}").easyWizard({
                 submitButton: false,
                 showButtons: false,
                 clickableStep: "true"
            });
            $('#${elementParamName!} > .easyWizardSteps > li').bind('click', function(e) {
                $("#${elementParamName!}").easyWizard('goToStep', $(this).data('step'));
            });
            $('#${elementParamName!} > .easyWizardWrapper > .step').each(function () {
                if ($(this).find(".easyWizardElement").length === 0) {
                    $(this).on("keydown", ":tabbable", function(e) {
                        var keyCode = e.keyCode || e.which;
                        if (keyCode === 9) {
                            var currentTab = $(this).closest('.step');
                            var tabbables = $(currentTab).find(':tabbable');
                            var first = tabbables.filter(':first');
                            var last  = tabbables.filter(':last');
                            var focusedElement = $(e.target);
                            var isFirstInFocus = (first.get(0) === focusedElement.get(0));
                            var isLastInFocus = (last.get(0) === focusedElement.get(0));
                            var tabbingForward = !e.shiftKey;
                            if (tabbingForward) {
                                var lastTab = $(currentTab).closest('.easyWizardElement').find(".easyWizardSteps li:visible:last").hasClass("current");
                                if (isLastInFocus && !lastTab) {
                                    $("#${elementParamName!}").easyWizard('nextStep');
                                    e.preventDefault();
                                }
                            } else {
                                var firstTab = $(currentTab).closest('.easyWizardElement').find(".easyWizardSteps li:visible:first").hasClass("current");
                                if (isFirstInFocus && !firstTab) {
                                    $("#${elementParamName!}").easyWizard('prevStep');
                                    e.preventDefault();
                                }
                            }
                        }
        });
                }
            });
        });
    </script>
    <#if !(request.getAttribute("org.joget.plugin.SectionTabs")??) >
        <script type="text/javascript" src="${request.contextPath}/plugin/org.joget.plugin.SectionTabs/js/jquery.easyWizard.js"></script>
        <style type="text/css">
            .tabs > .easyWizardSteps {list-style:none;width:100%;overflow:hidden;margin:0;padding:0;border-bottom:1px solid #ccc;margin-bottom:20px;}
            .tabs > .easyWizardSteps > li {font-size:18px;display:inline-block;padding:10px;color:#B0B1B3;margin-right:20px;}
            .tabs > .easyWizardSteps > li span {font-size:24px;}
            .tabs > .easyWizardSteps li.error {color:red !important;}
            .tabs > .easyWizardSteps > li.current {color: #000; background: #eee; border-top-left-radius: 10px; border-top-right-radius: 10px; font-weight: bold;}
            .tabs > .easyWizardWrapperContainer > .easyWizardWrapper:after{content:""; clear:both;}
            .tabs > .easyWizardWrapper > .step.active {visibility: unset !important}
            .tabs > .easyWizardWrapper > .step:not(.active) > .form-section-title {display: block !important; padding: 1px;}
            .tabs > .easyWizardWrapper > .step:not(.active) > .form-column {display: none}
            .tabs > .easyWizardWrapper > .step:not(.active) > .subform-section-title {display: block !important; padding: 1px;}
            .tabs > .easyWizardWrapper > .step:not(.active) > .subform-column {display: none}
            .tabs > .easyWizardWrapper > .step{visibility: hidden; display:block !important; padding:0px !important; border:0px !important; clear: right !important; margin:0 !important; margin-top:0px !important; box-shadow:none !important; }
            .tabs > .easyWizardWrapper > .step:after {content:""; display:table; clear:both;}
            .tabs > .easyWizardWrapperContainer > .easyWizardWrapper > .step_wrapper > .step{display:block !important; padding:0px !important;  margin:0 !important; margin-top:0px !important; box-shadow:none !important; clear: right !important; border: 0px !important;}
            .tabs.rtl > .easyWizardWrapper > .step.active {float: right !important;}
            .tabs .step.section-visibility-hidden {display: none !important; visibility: hidden !important;}
            .tabs.easyWizardElement.easyPager {overflow-x: clip !important;}
            .tabs > .easyWizardWrapper.sliding {transition: transform 400ms ease-in-out;}

            </style>
        <script>
          $(document).ready(function(){
    var originalChange = VisibilityMonitor.prototype.triggerChange;

    VisibilityMonitor.prototype.triggerChange = function(targetEl, names) {
        originalChange(targetEl, names);
        $(targetEl).trigger("section-visibility-control-changed");
    };
    if (${rightToLeft?string}) {
        $("#${elementParamName!}").addClass("rtl");
    }
});
        </script>
    </#if>
</#if>