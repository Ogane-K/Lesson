const stripe = Stripe('pk_test_51RYl7bBBdYnXts63x2L6fgDl6bxh7lnOuFdPqCaz3arOhZ13LReDQcCt9ZnXLCg3cSBf70hkHcUO9nHSIKWUhBPy008HEcA8M1');

const paymentButton = document.querySelector('#paymentButton');

paymentButton.addEventListener('click',() => {
    stripe.redirectToCheckout({
        sessionId: sessionId
    })
});