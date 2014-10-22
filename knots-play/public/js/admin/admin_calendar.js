/**
 * Created by anton on 10/20/14.
 */

$(function () {
//    $.ajax({
//        beforeSend: function (xhr) {                   // Before requesting data
//            if (xhr.overrideMimeType) {                 // If supported
//                xhr.overrideMimeType("application/json"); // set MIME to prevent errors
//            }
//        }
//    });

    function loadMasseurs() {
        $.getJSON('masseurs/list/json')
            .done(function(data) {
                console.log('loaded masseurs');
                $.each(data.masseurs, function(i, obj) {
                    storeMasseur(obj);
                    console.log('Stored a masseur ' + obj.firstName);
                });
            })
            .fail(function() {

            })
    }

    function storeMasseur(masseur) {
        var masseurListContainer = $('#masseurs-container');
        var el = $('<div class="external-event label-primary" data-eventclass="label-primary">' + masseur.firstName + ' ' + masseur.lastName + '</div>');
        masseurListContainer.append(el);
        var eventObject = {
            title: masseur.firstName + " " + masseur.lastName,
            masseurId: masseur.id
        };
        el.data('eventObject', eventObject);
        el.draggable({
            zIndex: 999,
            revert: true,      // will cause the event to go back to its
            revertDuration: 0  //  original position after the drag
        });
    }

    function initCalendar() {
        var date = new Date();
        var d = date.getDate();
        var m = date.getMonth();
        var y = date.getFullYear();

        var calendar = $('#calendar').fullCalendar({
            header: {
                left: 'prev,next today',
                center: 'title',
                right: 'agendaWeek,month'
            },
            selectable: false,
            selectHelper: false,
            weekends: false,
            editable: true,
            lazyFetching: true,
            droppable: true, // this allows things to be dropped onto the calendar !!!
            drop: function (date, allDay) { // this function is called when something is dropped

                // retrieve the dropped element's stored Event Object
                var originalEventObject = $(this).data('eventObject');

                // we need to copy it, so that multiple events don't have a reference to the same object
                var copiedEventObject = $.extend({}, originalEventObject);

                // assign it the date that was reported
                copiedEventObject.start = date;
                copiedEventObject.allDay = allDay;

                // copy label class from the event object
                var labelClass = $(this).data('eventclass');

                if (labelClass) {
                    copiedEventObject.className = labelClass;
                }

                // render the event on the calendar
                // the last `true` argument determines if the event "sticks" (http://arshaw.com/fullcalendar/docs/event_rendering/renderEvent/)
                $('#calendar').fullCalendar('renderEvent', copiedEventObject, false);
                reserveMasseur(date, originalEventObject.masseurId);
            },
            events: '/admin/schedule'
        });
    };

    function reserveMasseur(date, masseurId) {
        console.log(date);
        console.log(date.toString());
        var data = JSON.stringify({ date : date, masseurId : masseurId});
        console.log(data);
        $.ajax({
            type: "POST",
            url: '/admin/work',
            dataType: 'application/json; charset=utf-8',
            contentType: "application/json; charset=utf-8",
            data: data,
            success: function() {
                alert('it worked!');
            }
        });
    };

    initCalendar();
    loadMasseurs();
});