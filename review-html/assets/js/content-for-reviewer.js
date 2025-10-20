$(document).ready(function() {
	createSeveritySelectors();
	$('.severity-selector').live('click', function() {
		$('#wrapper ul li.taggable').show();
		$('.severity-selector').removeClass('selected');
		$(this).addClass('selected');
		if($(this).attr('data-severity-selector').length > 0) {
			$('#wrapper ul li.taggable').not("li[data-severity=\"" + $(this).attr('data-severity-selector') + "\"]").hide();
		}
	});
});

function createSeveritySelectors() {
	tags = new Array();
	tagElement = $('<div id="severity-selectors"><strong>Filter by</strong><span class="severity-selector selected" data-tag-selector="">No filter</span></div>')
	$('#wrapper ul li[data-severity]').each(function() {
		tagName = $(this).attr('data-severity');
		if(tagName != "null" && $.inArray(tagName, tags) == -1) {
			tags.push(tagName);
			$('<span class="severity-selector" data-severity-selector="' + tagName + '">' + tagName + '</span>').appendTo(tagElement);
		}
	})
	$('body').append(tagElement)
}