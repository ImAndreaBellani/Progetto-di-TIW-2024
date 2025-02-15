/**
 * script atto a processare le informazioni inserite nel form di login prima dell'invio dei dati al server.
 */
(function() {
	let error_box = new ErrorParagraphContent();
	error_box.hide();
	let on_click_listener = (e)=>{
		e.preventDefault();
		let form = e.target.closest("form");
	    if (checkRegistrationFields(form, error_box))
	    	{
	      		sendAsynRequest("POST", '/TIW-GestioneDiImmagini(con JavaScript)/DoLogin', e.target.closest("form"), function(x) {
	          	if (x.readyState == XMLHttpRequest.DONE)
	          		{
	            		switch (x.status)
	            			{
								case 200:
									localStorage.setItem('username', x.responseText);
								    error_box.hide();
								    window.location.href = "home.html";
								    break;
								default:
									error_box.update(x.responseText);
								    break;
	            			}
	          		}
				});
	    	}
	    else
	    	{
	    		form.reset();
	    	}		
	};		
  	
  	document.getElementById("login_button").addEventListener('click', on_click_listener);
  	function checkRegistrationFields(form, error_box) {	
		return (
			checkUsername(form.username.value, error_box) &&
			checkPassword(form.password.value, error_box)
		);
	}
	function checkUsername (username, error_box) {
		if (username == null || username == "")
			{
				error_box.update("il campo username non puo' essere vuoto");
				return (false);
			}
		if (Array.from(username).length>50)
			{
				error_box.update("la dimensione massima per lo username e' di 50 caratteri");
				return (false);
			}
		
		return (true);
	}
	function checkPassword (password, error_box) {
		if (password == null || password == "")
			{
				error_box.update("il campo password non puo' essere vuoto");
				return (false);
			}
		if (Array.from(password).length>50)
			{
				error_box.update("la dimensione massima per la password e' di 50 caratteri");
				return (false);
			}
		
		return (true);
	}
	function ErrorParagraphContent() {
		this.update = function (message) {
			document.getElementById("content_error_paragraph").innerHTML = message;
			document.getElementById("body_canvas").setAttribute("style", "display:inline");	
			document.getElementById("error_paragraph").setAttribute("style", "display:table");		
		}
		this.hide = function () {
			document.getElementById("content_error_paragraph").innerHTML = "";
			document.getElementById("error_paragraph").setAttribute("style", "display:none");
			document.getElementById("body_canvas").setAttribute("style", "display:none");	
		};
		document.getElementById("body_canvas").addEventListener("click", this.hide);
	}
})();