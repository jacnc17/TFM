// Método para gestionar una posible sesión anterior
function get_sesion_anterior() {
    // alert (getCookie("id_proyecto") );
    if (getCookie("id_proyecto") == "")
        document.cookie = "id_proyecto";


    var id_proyecto = getCookie("id_proyecto");

    console.log ("id_proyecto ",id_proyecto);

    // Verificamos posible id de proyecto anterior
    if (id_proyecto == '' || id_proyecto == null || id_proyecto== undefined)
    {
        console.log ("No hay un id de proyecto");

/*         fetch("/getIdProyecto")
        .then(response => response.text())
        .then((response) => {
            document.cookie = "id_proyecto = " + response + "; expires=Thu, 1 Jan 2122 12:00:00 UTC";
        })
        .catch(err => console.log(err)) */
        id_proyecto = -1;
    }
    else // Ya había un proyecto anterior
    {
        console.log ("HABIA ANTES un id de proyecto : ", id_proyecto);
    }
        fetch("/actualizaIdProyecto/"+id_proyecto)
        .then(response => response.text())
        .then((response) => {
            document.cookie = "id_proyecto = " + response + "; expires=Thu, 1 Jan 2122 12:00:00 UTC";
        })
        .catch(err => console.log(err))
    
}




// Función para recuperar una cookie específica
// https://www.w3schools.com/js/js_cookies.asp
function getCookie(cname) {
    let name = cname + "=";
    let decodedCookie = decodeURIComponent(document.cookie);
    let ca = decodedCookie.split(';');
    for (let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}
