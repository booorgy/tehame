
function lazyThumbnails() {
	$('.lazy').lazy({
		scrollDirection: 'vertical',
		effect: 'fadeIn',
		visibleOnly: true,
		onError: function(element) { }
	});
}

$(function() {
	lazyThumbnails();
});
	