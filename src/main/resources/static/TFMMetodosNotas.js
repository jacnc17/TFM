// Método para añadir una cookie
function add_cookie_nota(id_nota, izquierda, arriba, tam, texto, color, clase, desde, hasta) {
    // Definición de variables.
    var fin = false;
    var indice = 0;
    var capa;

    while (!fin) {
        // console.log ("add_cookie_nota");

        var elto = getCookie("notas." + indice + ".id_nota");

        // Si la nota no existe, hemos llegado a la última.
        if (elto == null || elto == "" || elto == undefined) {
            fin = true;
        } else {
            // Si el identificador de la nota coincide con el buscado, se finaliza la búsqueda
            if (elto == id_nota)
                fin = true;
            else // Si el identificador de la nota no es el buscado
                indice++;
        }
    }

    // En este punto, indice contiene la posición de la nota (si estamos actualizando),
    // o la posición de la siguiente nota disponible si estamos creando una nueva, así como el resto de propiedades
    document.cookie = "notas." + indice + ".id_nota = " + id_nota + "; expires=Thu, 1 Jan 2122 12:00:00 UTC";
    document.cookie = "notas." + indice + ".izquierda = " + izquierda + "; expires=Thu, 1 Jan 2122 12:00:00 UTC";
    document.cookie = "notas." + indice + ".tipo_nota = 'txt' ; expires=Thu, 1 Jan 2122 12:00:00 UTC";
    document.cookie = "notas." + indice + ".arriba = " + arriba + "; expires=Thu, 1 Jan 2122 12:00:00 UTC";
    document.cookie = "notas." + indice + ".tam = " + tam + "; expires=Thu, 1 Jan 2122 12:00:00 UTC";
    document.cookie = "notas." + indice + ".texto = " + texto + "; expires=Thu, 1 Jan 2122 12:00:00 UTC";
    document.cookie = "notas." + indice + ".color = " + color + "; expires=Thu, 1 Jan 2122 12:00:00 UTC";
    document.cookie = "notas." + indice + ".clase = " + clase + "; expires=Thu, 1 Jan 2122 12:00:00 UTC";
    document.cookie = "notas." + indice + ".desde = " + desde + "; expires=Thu, 1 Jan 2122 12:00:00 UTC";
    document.cookie = "notas." + indice + ".hasta = " + hasta + "; expires=Thu, 1 Jan 2122 12:00:00 UTC";

}



// Método para registrar una cookie de una nota formada por una imagen
function add_cookie_imagen(id_nota, izquierda, arriba, tam, desde, hasta, imagen, tam_original){
    // Definición de variables.
    var fin = false;
    var indice = 0;
    var capa;

    while (!fin) {
        // console.log ("add_cookie_imagen");

        var elto = getCookie("notas." + indice + ".id_nota");

        // Si la nota no existe, hemos llegado a la última.
        if (elto == null || elto == "" || elto == undefined) {
            fin = true;
        } else {
            // Si el identificador de la nota coincide con el buscado, se finaliza la búsqueda
            if (elto == id_nota)
                fin = true;
            else // Si el identificador de la nota no es el buscado
                indice++;
        }
    }

    document.cookie = "notas." + indice + ".id_nota = " + id_nota + "; expires=Thu, 1 Jan 2122 12:00:00 UTC";
    document.cookie = "notas." + indice + ".tipo_nota = img ; expires=Thu, 1 Jan 2122 12:00:00 UTC";
    document.cookie = "notas." + indice + ".izquierda = " + izquierda + "; expires=Thu, 1 Jan 2122 12:00:00 UTC";
    document.cookie = "notas." + indice + ".arriba = " + arriba + "; expires=Thu, 1 Jan 2122 12:00:00 UTC";
    document.cookie = "notas." + indice + ".tam = " + tam + "; expires=Thu, 1 Jan 2122 12:00:00 UTC";
    document.cookie = "notas." + indice + ".desde = " + desde + "; expires=Thu, 1 Jan 2122 12:00:00 UTC";
    document.cookie = "notas." + indice + ".hasta = " + hasta + "; expires=Thu, 1 Jan 2122 12:00:00 UTC";
    document.cookie = "notas." + indice + ".src = " + imagen + "; expires=Thu, 1 Jan 2122 12:00:00 UTC";
    document.cookie = "notas." + indice + ".tam_original = " + tam_original + "; expires=Thu, 1 Jan 2122 12:00:00 UTC";
    
    // Borra los posibles atributos de la cookie de texto
    deleteGrupoCookies("notas." + indice + ".texto");
    deleteGrupoCookies("notas." + indice + ".color");
    deleteGrupoCookies("notas." + indice + ".clase");
    
}

// Función para recuperar las notas almacenadas en cookies. A esta función sólo se le llamará si se recarga la página.
function get_notas_cookies() {
    // Recojo las cookies de notas
    var fin = false;
    var indice = 0;
    var capa;


    // Se crea el div de notas, inicialmente con duración dummy (se actualizará cuando se muestren las notas)
    get_div_notas(-1);
    duracion_total_actual = 0;

    while (!fin) {
        var elto = getCookie("notas." + indice + ".id_nota");

        console.log ("get_notas_cookies");

        if (elto == null || elto == "" || elto == undefined) {
            fin = true;
        } else {
            // De la cookie se recupera la capa original       
            // console.log("Encontrada cookie ", elto);




            /////////////////// NOTAS 
            var nota = document.getElementById('plantillaNota').cloneNode(true); // true means clone all childNodes and all event handlers;
            nota.id = elto;
            // nota.className = "nota_oculta";
            nota.style.display = "inherit";
            nota.style.position = "absolute";
            nota.querySelector(".colorpick").id = nota.id + "_color";
            nota.getElementsByClassName("dropdown-menu")[0].setAttribute("nota", nota.id);
            nota.getElementsByClassName("barraMenu")[0].id = "barra_" + nota.id;

            // Creamos el recuadro con el texto de la nota.
            let recuadro_texto = nota.getElementsByClassName("clase_recuadro_texto")[0];


            // Se asignan las propiedades de las notas según lo almacenado en las cookies.
            recuadro_texto.id = "recuadro" + elto.substring(5); // La nota viene en forma "NOTA_" + timeStampInMs, y aquí queremos recuperar el timestamp
            recuadro_texto.setAttribute("nota_asociada", nota.id);
            recuadro_texto.style.fontSize = getCookie("notas." + indice + ".tam");
            recuadro_texto.style.color = getCookie("notas." + indice + ".color");
            recuadro_texto.className = getCookie("notas." + indice + ".clase");
            recuadro_texto.value = getCookie("notas." + indice + ".texto");

            // TODO: recuperar atributos de la cookie si es una imagen

            console.log ("hashPropiedadesNotas  =",hashPropiedadesNotas);
            console.log ("hashPropiedadesNotas desde =",getCookie("notas." + indice + ".desde"));
            console.log ("hashPropiedadesNotas hasta =",getCookie("notas." + indice + ".hasta"));

            // Se asigna el rango de cada nota según lo almacenado en las cookies.
            hashPropiedadesNotas.set("ini_"+nota.id, getCookie("notas." + indice + ".desde"));
            hashPropiedadesNotas.set("fin_"+nota.id, getCookie("notas." + indice + ".hasta"));
            console.log ("hashPropiedadesNotas  =",hashPropiedadesNotas);

            let barraMenu = nota.getElementsByClassName("barraMenu")[0];

            // Se ocultan todas las barras de menú para evitar confusión
            let todas_notas = document.getElementsByClassName ("barraMenu");
            for (i = 0; i<todas_notas.length; i++)
            {
                tmp_nota = todas_notas[i];
                // console.log ('ocultando nota ',tmp_nota);
                $(tmp_nota).removeClass("nota_activa"); $(tmp_nota).addClass("nota_oculta"); $(tmp_nota).css("zIndex", "0"); 
            }



            // Condiciones para mostrar los controles del texto. // ORIGINAL : $ nota).hover...
            $(barraMenu).hover(function () { 
                // Se ocultan todas las barras de menú para evitar confusión
                let todas_notas = document.getElementsByClassName ("barraMenu");
                for (i = 0; i<todas_notas.length; i++)
                {
                    tmp_nota = todas_notas[i];
                    // console.log ('ocultando nota ',tmp_nota);
                    $(tmp_nota).removeClass("nota_activa"); $(tmp_nota).addClass("nota_oculta"); $(tmp_nota).css("zIndex", "0"); 
                }

                $(this).removeClass("nota_oculta"); $(this).addClass("nota_activa"); $(this).css("zIndex", "100"); 
            }, 
            function () { 
                $(this).removeClass("nota_activa"); $(this).addClass("nota_oculta"); $(this).css("zIndex", "0");
            });


            // Se añade la nota
            document.getElementById("marco_oculto_notas").appendChild(nota);

            // Se restauran las propiedades de la nota (en las cookies)
            nota.style.left = getCookie("notas." + indice + ".izquierda");
            nota.style.top = getCookie("notas." + indice + ".arriba");

            // Se establece el punto de inicio
            // nota_set_inicio(0); // Empieza en la posición del slide
            // hashPropiedadesNotas.set("ini_" + nota.id, 0);
            // nota_set_fin_hasta_final(nota.id);  // Por defecto acaba en el fin del video.

            // Se hace foco en el recuadro de texto
            // 29ENE limita_ancho(recuadro_texto);
            // 29ENE recuadro_texto.focus();

            // Se recogen los cambios en el color picker
            nota.addEventListener("change", addNota_watch, false);
            nota.querySelector(".colorpick").setAttribute("input_asociado", "recuadro" +  elto.substring(5)); // La nota viene en forma "NOTA_" + timeStampInMs, y aquí queremos recuperar el timestamp);

            // Añade la capacidad de arrastre
            $(nota).draggable({
                drag: function (event, ui) {
                    limita_ancho(recuadro_texto);
                    var pos_y = ui.position.top + 32 ;
console.log ("29ENE: pos_y = ",pos_y);

                    if (pos_y < 0) // Ajuste considerando alto de la barra de menús.
                    {
    
                        ui.position.top = -32;
console.log ("29ENE: pos_y ACTUALIZADA! = ",pos_y);
                    }

   
                    // Determinamos si es un recuadro de texto
                    if (getCookie("notas." + indice + ".tipo_nota") == 'txt' )
                    {
    
                        // Recuperamos el id del cuadro de texto
                        let id_recuadro_texto = "recuadro"+$(nota)[0].id.substring(5); // El id de nota viene con prefijo NOTA_
                        let tam = document.getElementById(id_recuadro_texto).style.fontSize;
                        let color = document.getElementById(id_recuadro_texto).style.color;
                        let clase = document.getElementById(id_recuadro_texto).className;
                        let desde = hashPropiedadesNotas.get("ini_" + $(nota)[0].id);
                        let hasta = hashPropiedadesNotas.get("fin_" + $(nota)[0].id);
    
    
                        let texto =  document.getElementById(id_recuadro_texto).value;
                        add_cookie_nota ( $(nota)[0].id, ui.position.left+"px", ui.position.top+"px", tam, texto, color, clase, desde, hasta);
                    }
                    else if (getCookie("notas." + indice + ".tipo_nota") == 'img' ) // Es una imagen
                    {
                        // Recuperamos los atributos de la imagen
                        console.log ("get_notas_cookies: es imagen!");
                        let id_recuadro_imagen = $(nota)[0].id+"_imagen_nota";
                        let tam = document.getElementById(id_recuadro_imagen).style.maxHeight;
                        let desde = hashPropiedadesNotas.get("ini_" + $(nota)[0].id);
                        let hasta = hashPropiedadesNotas.get("fin_" + $(nota)[0].id);
                        let src =  document.getElementById(id_recuadro_imagen).src;
                        let tam_original = getCookie("notas." + indice + ".tam_original");

                        console.log ("get_notas_cookies. Actualizando cookie: ", id_recuadro_imagen, "  en rango (",desde,":",hasta,", src = ",src, ",tam = ", tam, ", tam_original =", tam_original);

                        add_cookie_imagen ( $(nota)[0].id, ui.position.left+"px", ui.position.top+"px", tam,  desde, hasta, src, tam_original);
                    }
                }
            });



            // Se determina si es una nota de texto o de imagen.
            if (getCookie("notas." + indice + ".tipo_nota") == 'img' )
            {
                // console.log ("get_notas_cookies: es imagen2 !");
                // Recuperamos las propiedades de la imagen 
                let imagen_original = getCookie("notas." + indice + ".src");
                let es_sticker = imagen_original.substring("imagenes/stickers/") != -1;
                let tamano =  getCookie("notas." + indice + ".tam");

                // Si hay un recuadro de texto asociado, se elimina.
                let id_recuadro_texto = "recuadro"+$(nota)[0].id.substring(5); // El id de nota viene con prefijo NOTA_
                if (document.getElementById(id_recuadro_texto) != null) {
                    document.getElementById(id_recuadro_texto).remove();
                }

                console.log ("recuperando imagen ", imagen_original, ", ", es_sticker);
                let _src = imagen_original.substring(getPosition(imagen_original, '/', 3)+1);
                
                incluye_imagen_nota(elto, _src, es_sticker) 
                
                // Se pone el tamaño correcto
                document.getElementById($(nota)[0].id+"_imagen_nota").style.maxHeight = tamano;

                // Indicamos el tamaño original
                let tam_original = getCookie("notas." + indice + ".tam_original");
                document.getElementById($(nota)[0].id+"_imagen_nota").setAttribute("tam_original", tam_original);
            }
            else 
            {
                console.log ("get_notas_cookies: NO! es imagen2 !", getCookie("notas." + indice + ".tipo_nota"));
            }

            indice++;
        }
    }
}


// Función auxiliar

function getPosition(string, subString, index) {
    return string.split(subString, index).join(subString).length;
  }
  

// Función para borrar un grupo de cookies específicas
function deleteGrupoCookies(prefijo) {
    let decodedCookie = decodeURIComponent(document.cookie);
    let ca = decodedCookie.split(';');
    for (let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(prefijo) == 0) {
            // return c.substring(name.length, c.length);
            // console.log ("deleteGrupoCookies: borrando cookie",c.substring(0, prefijo.length));
            document.cookie = c + "=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";

        }
    }
}

// Función que actualizará los atributos de las notas en las cookies
function almacena_notas_cookies ()
{
    let notas = document.getElementById("marco_oculto_notas");

    deleteGrupoCookies("notas.");

    // Si hay alguna nota
    if (notas != null && notas != undefined) {

        // 29ENE console.log ("ALMACENA NOTAS COOKIES");



        // Definición de variables.
        var fin = false;
        var indice = 0;
        var cont = 0;
        var capa;

        // 29ENE console.log ("ALMACENA NOTAS COOKIES2");

        // Se van recorriendo todas las notas
        while (indice < notas.childNodes.length) {
            // 29ENE console.log ("ALMACENA NOTAS COOKIES3");

            // Evitamos la imagen temporal
            if (notas.childNodes[indice].id != "img_temporal") {
                // Recuperamos el id de la nota y su posición.
                let id_nota = notas.childNodes[indice].id;
                let izquierda = notas.childNodes[indice].style.left ;
                let arriba = notas.childNodes[indice].style.top ;
                let desde = hashPropiedadesNotas.get("ini_" +id_nota);
                let hasta = hashPropiedadesNotas.get("fin_" + id_nota);

                // Verificamos si se trata de una imagen
                if (document.getElementById(id_nota+"_imagen_nota") != null)
                {
                    // Recuperamos los atributos exclusivos de las imágenes.
                    let tam = document.getElementById(id_nota+"_imagen_nota").style.maxHeight;
                    let tam_original = document.getElementById(id_nota+"_imagen_nota").getAttribute("tam_original");


                    add_cookie_imagen(id_nota, izquierda, arriba, tam, desde, hasta, document.getElementById(id_nota+"_imagen_nota").src, tam_original);
                }
                else // Se trata de un recuadro de texto
                {
                    // Recuperamos el id del cuadro de texto y los atributos de los textos
                    let id_recuadro_texto = "recuadro" + id_nota.substring(5); // El id de nota viene con prefijo NOTA_
                    let tam = document.getElementById(id_recuadro_texto).style.fontSize;
                    let color = document.getElementById(id_recuadro_texto).style.color;
                    let clase = document.getElementById(id_recuadro_texto).className;

                    let texto =  document.getElementById(id_recuadro_texto).value;

                    add_cookie_nota(id_nota, izquierda, arriba, tam, texto, color, clase, desde, hasta);
                }

                // document.cookie = "notas." + cont + ".id_nota = " + notas.childNodes[indice].id + "; expires=Thu, 1 Jan 2122 12:00:00 UTC";
                // document.cookie = "notas." + cont + ".izquierda = " + notas.childNodes[indice].style.left + "; expires=Thu, 1 Jan 2122 12:00:00 UTC";
                // document.cookie = "notas." + cont + ".arriba = " + notas.childNodes[indice].style.top + "; expires=Thu, 1 Jan 2122 12:00:00 UTC";

                // 29ENE console.log ("almacena_notas_cookies = ",notas);
                cont++;
            }

                indice++;


            // 29ENE console.log ("almacena_notas_cookies");

        }

    }

    // console.log ("almacena_notas_cookies = ",notas);
}




// Configura y abre el color picker
function colorea (id_nota)
{
    // alert (id_nota);
    let id_recuadro_texto = "recuadro" + id_nota.substring(5); // El id de nota viene con prefijo NOTA_
    // let color_actual = document.getElementById(id_recuadro_texto).style.color;
    let color_actual = document.getElementById(id_recuadro_texto).getAttribute("colorHEX");

    // Se le indica al picker qué recuadro se está editando
    cpGeneral.setAttribute("id_recuadro_texto",id_recuadro_texto);
    console.log("colorea: color = ",color_actual);


    if (color_actual == null || color_actual == '') 
        color_actual = "#FFFFFF";
    
    // Se pone en el picker el color
    console.log("colorea: color = ",color_actual);
    cpGeneral.value = color_actual;

    cpGeneral.click();
}