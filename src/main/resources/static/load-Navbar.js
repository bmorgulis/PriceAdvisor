//This script is used to load the navbar on every page


document.addEventListener("DOMContentLoaded", () => {       //adds an event listener to the entire page
    fetch('navbar.html')                                //fetches file(navbar.html) we will use to load the navbar
        .then(response => response.text())          //converts the response to text
        .then(data => {                                 //take data and put it on current page
            document.getElementById('navbar').innerHTML = data;
        })
        .catch(error => console.error('Error: cannot load navbar for some reason:', error));
});