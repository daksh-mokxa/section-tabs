/* ========================================================
 * easyWizard v1.1.3
 * http://st3ph.github.com/jquery.easyWizard
 * ========================================================
 * Copyright 2012 - 2015 Stéphane Litou
 * http://stephane-litou.com
 *
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.opensource.org/licenses/GPL-2.0
 * ======================================================== */
(function( $ ) {
    var arrSettings = [];
    var easyWizardMethods = {
        init : function(options) {
            var settings = $.extend( {
                'stepClassName' : 'step',
                'showSteps' : true,
                'stepsText' : '{n}. {t}',
                'showButtons' : true,
                'buttonsClass' : '',
                'prevButton' : '< Back',
                'nextButton' : 'Next >',
                'debug' : false,
                'submitButton': true,
                'submitButtonText': 'Submit',
                'submitButtonClass': '',
                'rightToLeft': false, 

                before: function(wizardObj, currentStepObj, nextStepObj) {},
                after: function(wizardObj, prevStepObj, currentStepObj) {},
                beforeSubmit: function(wizardObj) {
                    wizardObj.find('input, textarea').each(function() {
                        if(!this.checkValidity()) {
                            this.focus();
                            step = $(this).closest('.'+thisSettings.stepClassName).attr('data-step');
                            easyWizardMethods.goToStep.call(wizardObj, step);

                            return false;
                        }
                    });
                }
            }, options);

            arrSettings[this.index()] = settings;
            
            $(window).resize(function () {
                $('.easyWizardElement').each(function(){
                    if($(this).parent().width() !=0){
                        var width = $(this).parent().width();
                        $(this).css("max-width", width);
                        $(this).find('.easyWizardWrapper').width(width * $(this).find('.easyWizardSteps')[0].childElementCount);
                        $(this).find('.step').each(function (i, obj) {
                            $(obj).width(width);
                        });
                        var currentStep = $(this).find('.easyWizardSteps > .current').attr('data-step');
                        var currentMarginLeft = width * (currentStep - 1) * -1;
                        $(this).find('.easyWizardWrapper').css("margin-left",currentMarginLeft);
                    }
                });                
            });
           
            return this.each(function() {
                thisSettings = settings;

                $this = $(this); // Wizard Obj
                $this.addClass('easyWizardElement');
                $steps = $this.find('> .'+thisSettings.stepClassName);
                thisSettings.steps = $steps.length;
                thisSettings.width = $(this).width();

                if(thisSettings.steps > 1) {
                    // Create UI
                    $this.wrapInner('<div class="easyWizardWrapper" />');
                    $this.find('.easyWizardWrapper').width(thisSettings.width * thisSettings.steps);
                    $this.css({
                        'position': 'relative'
                    }).addClass('easyPager');

                    $stepsHtml = $('<ul class="easyWizardSteps">');

                 $steps.each(function(index) {
                        step = index + 1;
                        var floatDirection = thisSettings.rightToLeft ? 'right' : 'left';
                        $(this).css({
                            'float': floatDirection,
                            'width': thisSettings.width,
                            'height': 'auto'
                        }).attr('data-step', step);

                        if(!index) {
                            $(this).addClass('active').css('height', '');
                        }else {
                            $(this).find('input, textarea, select, button').attr('tabindex', '-1');
                        }

                        //stepText = thisSettings.stepsText.replace('{n}', '<span>'+step+'</span>');
                        //stepText = stepText.replace('{t}', $(this).attr('data-step-title'));
                        stepText = $(this).find("> .form-section-title span, > .subform-section-title span").text();
                        $stepsHtml.append('<li' + (!index ? ' class="current"' : '') + ' data-step="' + step + '">' + stepText + '</li>');
                    });

                    if(thisSettings.showSteps) {
                        $this.prepend($stepsHtml);
                    }

                    //hide hidden step
                    $steps.each(function(index) {
                        easyWizardMethods.updateStep.call($this, this, false);

                        $(this).on('section-visibility-control-changed', function() {
                            easyWizardMethods.updateStep.call($this, this, true);
                        })
                    });
                    
                    if(thisSettings.showButtons) {
                        paginationHtml = '<div class="easyWizardButtons">';
                            paginationHtml += '<button class="prev '+thisSettings.buttonsClass+'">'+thisSettings.prevButton+'</button>';
                            paginationHtml += '<button class="next '+thisSettings.buttonsClass+'">'+thisSettings.nextButton+'</button>';
                            paginationHtml += thisSettings.submitButton?'<button type="submit" class="submit '+thisSettings.submitButtonClass+'">'+thisSettings.submitButtonText+'</button>':'';
                        paginationHtml  += '</div>';
                        $paginationBloc = $(paginationHtml);
                        $paginationBloc.css('clear', 'both');
                        $paginationBloc.find('.prev, .submit').hide();
                        $paginationBloc.find('.prev').bind('click.easyWizard', function(e) {
                            e.preventDefault();

                            $wizard = $(this).closest('.easyWizardElement');
                            easyWizardMethods.prevStep.apply($wizard);
                        });

                        $paginationBloc.find('.next').bind('click.easyWizard', function(e) {
                            e.preventDefault();

                            $wizard = $(this).closest('.easyWizardElement');
                            easyWizardMethods.nextStep.apply($wizard);
                        });
                        $this.append($paginationBloc);

                        easyWizardMethods.updateButtons.call($this);
                    }

                    $formObj = $this.is('form')?$this:$(this).find('form');

                    // beforeSubmit Callback
                    $this.find('[type="submit"]').bind('click.easyWizard', function(e) {
                        $wizard = $(this).closest('.easyWizardElement');
                        var beforeSubmitValue = thisSettings.beforeSubmit($wizard);
                        if(beforeSubmitValue === false) {
                            return false;
                        }
                        return true;
                    });
                    
                    if (thisSettings.clickableStep === "true") {
                        $this.find("> ul.easyWizardSteps > li").css("cursor", "pointer");

                        $this.find("> ul.easyWizardSteps > li").click( function(){
                            $(this).closest(".easyWizardElement").easyWizard('goToStep', $(this).attr("data-step"));
                        });
                    }

                    //check for validation error
                    var error = -1;
                    $steps.each(function(){
                        if ($(this).hasClass("error")) {
                            if (error === -1) {
                                error = $(this).data("step");
                            }
                            $this.find("> ul.easyWizardSteps > li[data-step='"+$(this).data("step")+"']").addClass("error");
                        }
                    });
                    if (error !== -1) {
                        easyWizardMethods.goToStep.call($this, error);
                    }

                }else if(thisSettings.debug) {
                    console.log('Can\'t make a wizard with only one step oO');
                }
            });
        },

        updateStep: function (stepEL, updateButton) {
            thisSettings = arrSettings[this.index()];
            var step = $(stepEL).data("step");

            if ($(stepEL).hasClass('section-visibility-hidden')) {
                this.find("> .easyWizardSteps > li[data-step='" + step + "']").hide();
            } else {
                this.find("> .easyWizardSteps > li[data-step='" + step + "']").show();

                if (updateButton) {
                    easyWizardMethods.goToStep.call(this, step);
                } else {
                    var $currentActive = this.find('> .easyWizardWrapper > .step.active:not(.section-visibility-hidden)');
                    if ($currentActive.length === 0) {
                        easyWizardMethods.goToStep.call(this, step);
                    }
                }
            }

            if (updateButton) {
                easyWizardMethods.updateButtons.call(this);
            }
        },
        updateButtons : function () {
            thisSettings = arrSettings[this.index()];

            // Define buttons
            var $paginationBloc = this.find('> .easyWizardButtons');
            if($paginationBloc.length) {
              if (this.find('> .easyWizardWrapper > .'+ thisSettings.stepClassName +':not(.section-visibility-hidden)').length === 1) {
                  $paginationBloc.find('.prev, .next').hide();
              } else {
                  $activeStep = this.find('> .easyWizardWrapper > .'+ thisSettings.stepClassName +'.active');
                  $paginationBloc.find('.prev, .next').hide();

                  var $prev = $activeStep.prev();
                  while($prev.hasClass("section-visibility-hidden") && $prev.hasClass(thisSettings.stepClassName)){
                      $prev = $prev.prev();
                  }
                  if ($prev.hasClass(thisSettings.stepClassName)) {
                      $paginationBloc.find('.prev').show();
                  }

                  var $next = $activeStep.next();
                  while($next.hasClass("section-visibility-hidden") && $next.hasClass(thisSettings.stepClassName)){
                      $next = $next.next();
                  }
                  if ($next.hasClass(thisSettings.stepClassName)) {
                      $paginationBloc.find('.next').show();
                  }
              }
            }
        },
        prevStep : function( ) {
            thisSettings = arrSettings[this.index()];
            $activeStep = this.find('> .easyWizardWrapper > .'+ thisSettings.stepClassName +'.active');
            var $prev = $activeStep.prev();
            while($prev.hasClass("section-visibility-hidden") && $prev.hasClass(thisSettings.stepClassName)){
                $prev = $prev.prev();
            }
            if ($prev.hasClass(thisSettings.stepClassName)) {
                prevStep = parseInt($prev.attr('data-step'));
                easyWizardMethods.goToStep.call(this, prevStep);
            }
        },
        nextStep : function( ) {
            thisSettings = arrSettings[this.index()];
            $activeStep = this.find('> .easyWizardWrapper > .'+ thisSettings.stepClassName +'.active');
            var $next = $activeStep.next();
            while($next.hasClass("section-visibility-hidden") && $next.hasClass(thisSettings.stepClassName)){
                $next = $next.next();
            }
            if ($next.hasClass(thisSettings.stepClassName)) {
                nextStep = parseInt($next.attr('data-step'));
                easyWizardMethods.goToStep.call(this, nextStep);
            }
        },
        goToStep : function(step) {
    thisSettings = arrSettings[this.index()];
    $activeStep = this.find('> .easyWizardWrapper > .'+ thisSettings.stepClassName +'.active');
    $nextStep = this.find('> .easyWizardWrapper > .'+thisSettings.stepClassName+'[data-step="'+step+'"]');
    currentStep = $activeStep.attr('data-step');

    // Prevent sliding same step
    if (currentStep == step) return;

    // Before callBack
    var beforeValue = thisSettings.before(this, $activeStep, $nextStep);
    if(beforeValue === false) {
        return false;
    }

   //Calculate position among visible steps only
    var $visibleSteps = this.find('> .easyWizardWrapper > .'+ thisSettings.stepClassName +':not(.section-visibility-hidden)');
    var targetVisibleIndex = 0;
    
    $visibleSteps.each(function(index) {
        if ($(this).attr('data-step') == step) {
            targetVisibleIndex = index;
            return false;
        }
    });

    var visibleCount = $visibleSteps.length;
    this.find('.easyWizardWrapper').width(thisSettings.width * visibleCount);

    // Slide !
    wizard = this;
    $activeStep.removeClass('active');
    $activeStep.find('input, textarea, select, button').attr('tabindex', '-1');

    $nextStep.css('height', '').addClass('active');
    $nextStep.find('input, textarea, select, button').removeAttr('tabindex');
    
    $nextStep.trigger('section_wizard_step_shown');
    
    var width = $($activeStep).width();
    
    wizard.css({ overflow: 'hidden' });
    this.find('> .easyWizardWrapper').stop(true, true).animate({
        'margin-left': width * targetVisibleIndex * -1  // ? Fixed calculation
    }, function () {
                //$activeStep.css({ height: '1px' });
        wizard.css({ overflow: 'unset' });
    });

    // Defines steps
    this.find('> .easyWizardSteps .current').removeClass('current');
    this.find('> .easyWizardSteps li[data-step="'+step+'"]').addClass('current');

    easyWizardMethods.updateButtons.call(this);

    // After callBack
    thisSettings.after(this, $activeStep, $nextStep);
}
    };

    $.fn.easyWizard = function(method) {
        if ( easyWizardMethods[method] ) {
            return easyWizardMethods[ method ].apply( this, Array.prototype.slice.call( arguments, 1 ));
        } else if ( typeof method === 'object' || ! method ) {
            return easyWizardMethods.init.apply( this, arguments );
        } else {
            $.error( 'Method ' +  method + ' does not exist on jQuery.easyWizard' );
        }
    };
})(jQuery);