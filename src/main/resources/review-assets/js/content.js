$(document).ready(function() {
	$('h3.foldable').click(function() {
		$(this).next('ul').slideToggle("fast");
		$(this).toggleClass('unfold');
	});
	
	$(window).scroll(function() {
		if($(window).scrollTop() > 0) {
			$('#scroll-top').fadeIn();
		} else {
			$('#scroll-top').fadeOut();
		}
	});

	addScrollButton();
	$('#scroll-top').click(function() {
		$("body").animate({scrollTop: 0}, "slow");
	});

	addFoldUnfoldButtons();
	$('#fold-controls .fold').click(function() {
		$('h3.foldable.unfold').click();
	});

	$('#fold-controls .unfold').click(function() {
		$('h3.foldable').not('.unfold').click();
	});
	
	$('#wrapper ul li.taggable').mouseenter(function() {
		if(!$(this).hasClass('current_snippet')) {
			$('.show_snippet').removeClass('current_snippet');
			$('.current_line').removeClass('current_line');
			showSnippet($(this).find('.snippet'));
			$(this).find('.show_snippet').addClass('current_snippet');
			$(this).addClass('current_line');
		}
	})

	createTagSelectors();
	$('.tag-selector').live('click', function() {
		$('#wrapper ul li.taggable').show();
		$('.tag-selector').removeClass('selected');
		$(this).addClass('selected');
		if($(this).attr('data-tag-selector').length > 0) {
			$('#wrapper ul li.taggable').not("li[data-tag=\"" + $(this).attr('data-tag-selector') + "\"]").hide();
		}
	});
	
	unfoldFirstFile();
	createSnippetViewer();
	
});

function addScrollButton() {
	$('body').append('<div id="scroll-top"></div>');
}

function createTagSelectors() {
	tags = new Array();
	tagElement = $('<div id="tag-selectors"><strong>Filter by</strong><span class="tag-selector selected" data-tag-selector="">No filter</span></div>')
	$('#wrapper ul li.taggable').each(function() {
		tagName = $(this).attr('data-tag');
		if(tagName != "null" && $.inArray(tagName, tags) == -1) {
			tags.push(tagName);
			$('<span class="tag-selector" data-tag-selector="' + tagName + '">' + tagName + '</span>').appendTo(tagElement);
		}
	})
	$('body').append(tagElement)
}

function unfoldFirstFile() {
	$('h3.foldable').first().click();
}

function addFoldUnfoldButtons() {
	$('body').append("<div id='fold-controls'><span class='fold'>Fold all</span>&nbsp;/&nbsp;<span class='unfold'>Unfold all</span></div>");
}

function createSnippetViewer() {
	$('<div id="snippet_viewer"><span class="intro">Passez votre souris sur une loupe pour voir un snippet de code</span></div>').appendTo('body');
}

function showSnippet(el) {
	$('#snippet_viewer').html(el.html());
}

SyntaxHighlighter.all();
