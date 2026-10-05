(function () {
    "use strict";
    document.querySelectorAll(".quantity-control").forEach(function (control) {
        var input = control.querySelector("input");
        var buttons = control.querySelectorAll("[data-quantity-step]");
        function limits() {
            var value = Number(input.value);
            buttons.forEach(function (button) { button.disabled = Number(button.dataset.quantityStep) < 0 ? value <= Number(input.min) : value >= Number(input.max); });
        }
        input.addEventListener("input", limits); input.addEventListener("change", limits); limits();
        buttons.forEach(function (button) { button.addEventListener("click", function () {
            var current = Number(input.value);
            if (!Number.isInteger(current)) current = Number(input.min);
            var next = Math.max(Number(input.min), Math.min(Number(input.max), current + Number(button.dataset.quantityStep)));
            input.value = String(next);
            input.dispatchEvent(new Event("change", { bubbles: true }));
        }); });
    });
    document.querySelectorAll("form[data-confirm]").forEach(function (form) {
        form.addEventListener("submit", function (event) {
            if (!window.confirm(form.dataset.confirm)) event.preventDefault();
        });
    });
    var checkout = document.querySelector("[data-checkout-form]");
    if (checkout) {
        var submit = checkout.querySelector("[data-place-order]");
        if (submit) {
            var label = submit.textContent;
            checkout.addEventListener("submit", function () {
                submit.disabled = true;
                submit.textContent = "Đang xác nhận đơn hàng…";
            });
            window.addEventListener("pageshow", function () { submit.disabled = false; submit.textContent = label; });
        }
    }
})();
