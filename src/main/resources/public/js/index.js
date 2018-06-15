var working = false;
$('.login').on('submit', function(e) {
  e.preventDefault();
  if (working) return;
  working = true;
  var $this = $(this),
    $state = $this.find('button > .state');
  $this.addClass('loading');
  $state.html('Authenticating');
  setTimeout(function() {
    $this.addClass('ok');
    $state.html('Welcome back!');
    setTimeout(function() {
      $state.html('Log in');
      $this.removeClass('ok loading');
      working = false;
    }, 4000);
  }, 3000);
});

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
