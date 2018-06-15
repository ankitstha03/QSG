$('#fox-popup-triger').on('click', function () {
    $('.fox-popup-wrap').fadeIn(2000);
    $("body").css("overflow", "hidden");
    return false;
});
$('.fox-close-btn').on('click', function () {
    $('.fox-popup-wrap').hide();
    $("body").css("overflow", "auto");
    return false;
});