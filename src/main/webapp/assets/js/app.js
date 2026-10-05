(function () {
    "use strict";
    var button = document.querySelector("[data-nav-toggle]");
    var navigation = document.querySelector("[data-main-nav]");
    if (button && navigation) {
        button.addEventListener("click", function () {
            var open = navigation.classList.toggle("open");
            button.setAttribute("aria-expanded", String(open));
        });
        document.addEventListener("keydown", function (event) {
            if (event.key === "Escape") { navigation.classList.remove("open"); button.setAttribute("aria-expanded", "false"); }
        });
    }
    document.querySelectorAll("img[data-fallback]").forEach(function (image) {
        image.addEventListener("error", function () {
            if (image.dataset.fallback && !image.dataset.fallbackUsed) {
                image.dataset.fallbackUsed = "true";
                image.src = image.dataset.fallback;
            }
        });
    });
    document.querySelectorAll("[data-password-toggle]").forEach(function (toggle) {
        toggle.addEventListener("click", function () {
            var input = document.getElementById(toggle.getAttribute("aria-controls"));
            var show = input.type === "password";
            input.type = show ? "text" : "password";
            toggle.textContent = show ? "Ẩn" : "Hiện";
            toggle.setAttribute("aria-pressed", String(show));
            toggle.setAttribute("aria-label", show ? "Ẩn mật khẩu" : "Hiện mật khẩu");
        });
    });
    document.querySelectorAll("form[data-confirm-password]").forEach(function (form) {
        var password = form.querySelector("[name=password]");
        var confirmation = form.querySelector("[name=confirmPassword]");
        function validateMatch() { confirmation.setCustomValidity(confirmation.value && confirmation.value !== password.value ? "Mật khẩu xác nhận không khớp." : ""); }
        password.addEventListener("input", validateMatch);
        confirmation.addEventListener("input", validateMatch);
    });
})();
