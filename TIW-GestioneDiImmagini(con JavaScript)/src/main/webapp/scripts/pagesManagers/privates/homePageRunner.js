(function() {
	const basic_url = '/TIW-GestioneDiImmagini(con JavaScript)/';
	
	let pageOrchestrator = new PageOrchestrator();
	
	pageOrchestrator.start();
	
	function PageOrchestrator () {
		let viewObjects = {};
		let actualObject;
		
		/*
			Istanziazione dei "listeners directors"
		*/
		this.uploadImagePageListenersDirector = new UploadImagePageListenersDirector(this);
		this.imagePageListenersDirector = new ImagePageListenersDirector(this);
		this.albumPageListenersDirector = new AlbumPageListenersDirector(this);
		this.createAlbumPageListenersDirector = new CreateAlbumPageListenersDirector(this);
		this.sortAlbumPageListenersDirector = new SortAlbumPageListenersDirector(this);
		this.homepageListenersDirector = new HomepageListenersDirector(this);
		this.menubarListenersDirector = new MenubarListenersDirector(this);
		
		this.showErrorParagraph = function(message) {
			viewObjects.errorParagraphContent.update(message);	
		}
		this.addModalBoxListener = function(target, pageContent, index) {
			viewObjects.modalBoxContent.addListeners(target, pageContent, index);
		}
		this.updateAlbumPageContent = function (pageContent, index) {
			this.albumPageListenersDirector.updateContent(pageContent, index);
		}
		this.start = function () {
			/*
				Istanziazione dei view objects
			*/
			viewObjects.menubar = new Menubar(this);
			viewObjects.modalBoxContent = new ModalBoxContent(this);
			viewObjects.modalBoxContent.hide();
			viewObjects.errorParagraphContent = new ErrorParagraphContent();
			viewObjects.errorParagraphContent.hide();
			viewObjects.homePageContent = new HomePageContent(this);
			viewObjects.homePageContent.hide();
			viewObjects.uploadImageForm = new UploadImageForm(this);
			viewObjects.uploadImageForm.hide();
			viewObjects.createAlbumForm = new CreateAlbumForm(this);
			viewObjects.createAlbumForm.hide();
			viewObjects.imagePageContent = new ImagePageContent(this);
			viewObjects.imagePageContent.hide();
			viewObjects.albumForm = new AlbumForm(this);
			viewObjects.albumForm.hide();
			viewObjects.sortAlbumForm = new SortAlbumForm(this);
			viewObjects.sortAlbumForm.hide();
			
			actualObject=viewObjects.homePageContent;
			viewObjects.menubar.update();
			this.backToHome();
		};
		/*
			funzioni per l'orchestrazione dei contenuti presentati all'utente
		*/
		this.logout = function () {
			sendAsynRequest("GET", basic_url +'DoLogout', null, function(x) {
				if (x.readyState == XMLHttpRequest.DONE)
			        {
			        	localStorage.clear();
			        	window.location.href = "login.html";
			        }
			});
		}
		this.goToUploadImage = function() {
			actualObject.hide();
			viewObjects.menubar.showHomeButton();
			viewObjects.menubar.hideBackButton();
			viewObjects.modalBoxContent.clearListeners();
			viewObjects.modalBoxContent.hide();
			viewObjects.uploadImageForm.update();
			actualObject=viewObjects.uploadImageForm;
		}
		this.backToHome = function () {
			actualObject.hide();
			viewObjects.menubar.hideBackButton();
			viewObjects.menubar.hideHomeButton();
			viewObjects.modalBoxContent.clearListeners();
			viewObjects.modalBoxContent.hide();
			viewObjects.homePageContent.show();
			actualObject=viewObjects.homePageContent;
		}
		this.goToCreateAlbum = function () {
			actualObject.hide();
			viewObjects.menubar.showHomeButton();
			viewObjects.menubar.hideBackButton();
			viewObjects.modalBoxContent.clearListeners();
			viewObjects.modalBoxContent.hide();
			viewObjects.createAlbumForm.show();
			actualObject=viewObjects.createAlbumForm;
		}
	
		this.goToImagePage = function (name, creator, returnAddress="") {
			viewObjects.imagePageContent.show(creator, name);
			actualObject.hide();
			viewObjects.modalBoxContent.clearListeners();
			viewObjects.modalBoxContent.hide();
			viewObjects.menubar.showHomeButton();
			
			if (returnAddress!="")
				{
					viewObjects.menubar.hideBackButton();
					viewObjects.menubar.configureBackButton(creator, returnAddress);
					viewObjects.menubar.showBackButton();
				}
				
			actualObject=viewObjects.imagePageContent;		
		};
		this.goToAlbumPage = function (creator, name) {
			actualObject.hide();
			viewObjects.menubar.showHomeButton();
			viewObjects.menubar.hideBackButton();
			viewObjects.modalBoxContent.clearListeners();
			viewObjects.modalBoxContent.hide();
			viewObjects.albumForm.show(creator, name);
			actualObject=viewObjects.albumForm;
		};
		this.goToSortAlbumPage = function (user, name) {
			actualObject.hide();
			viewObjects.menubar.showHomeButton();
			viewObjects.menubar.hideBackButton();
			viewObjects.menubar.configureBackButton(user, name);
			viewObjects.menubar.showBackButton();
			viewObjects.modalBoxContent.clearListeners();
			viewObjects.modalBoxContent.hide();
			viewObjects.sortAlbumForm.show(user, name);
			actualObject=viewObjects.sortAlbumForm;
		};
	}
	function UploadImagePageListenersDirector (pageOrchestrator) {
		let uploadingImageCheck = function(form) {
			
			let error_message = "";
		
			if (form.name.value == null || form.name.value == "")
				{				
					error_message = "il nome dell'immagine e' un campo obbligatorio";
					pageOrchestrator.showErrorParagraph(error_message);
					return (false);
				}
			if (Array.from(form.name.value).length>50)
				{
					error_message = "la dimensione massima per il nome e' 50 caratteri";
					pageOrchestrator.showErrorParagraph(error_message);
					return (false);
				}
			if (form.description.value == null || form.description.value == "")
				{
					error_message = "la descrizione di un'immagine e' un campo obbligatorio";
					pageOrchestrator.showErrorParagraph(error_message);
					return (false);
				}
			if (Array.from(form.description.value).length>500)
				{
					error_message = "la dimensione massima per la descrizione e' 500 caratteri";
					pageOrchestrator.showErrorParagraph(error_message);
					return (false);
				}
			if (form.image.value == "")
				{
					error_message = "nessuna immagine selezionata";
					pageOrchestrator.showErrorParagraph(error_message);
					return (false);
				}
			
			return (true);
		};
		
		let uploadImageButtonListenerShape = (e) => {
			e.preventDefault();
			
			let form = e.target.closest("form");
			
			if (uploadingImageCheck(form))
				{
					sendAsynRequest("POST", basic_url+'DoUploadImage', e.target.closest("form"), function(x) {
					if (x.readyState == XMLHttpRequest.DONE)
						{					
					    	switch (x.status)
					        	{
									case 200:
										pageOrchestrator.backToHome();
										break;
									default:
										pageOrchestrator.showErrorParagraph(x.responseText);
										break;
					           	}
					    }
					});
				}		
		};
		
		this.addUploadImageButtonListener = function (target, event="click") {
			target.removeEventListener(event, uploadImageButtonListenerShape);
			target.addEventListener(event, uploadImageButtonListenerShape);
		}
	}
	function AlbumPageListenersDirector(pageOrchestrator) {
		let pageContent;
		let imagesContainer;
		let start;
		let previous_button;
		let next_button;
		
		let showNavigateButtons = function () { //ad ogni "aggiornamento" di "start" si controlla quali bottoni vanno visualizzati e quali no
			if (start==0)
				{
					previous_button.style.visibility = "hidden";
				}
			else
				{
					previous_button.style.visibility = "visible";
				}
				
			if (start+5>=pageContent.images.length)
				{
					next_button.style.visibility = "hidden";
				}
			else
				{	
					next_button.style.visibility = "visible";
				}
		}
		let sortButtonListenerShape = (e) => {
			e.preventDefault();
			pageOrchestrator.goToSortAlbumPage(pageContent.creator, pageContent.name);
		};
		let deleteButtonListenerShape = (e) => {
			e.preventDefault();
			let form = e.target.closest("form");
			sendAsynRequest("POST", basic_url+'DoDeleteAlbum', form, function(x) {
				if (x.readyState == XMLHttpRequest.DONE)
					{
						switch (x.status)
							{
								case 200:
									pageOrchestrator.backToHome();
									break;
								default:
									pageOrchestrator.showErrorParagraph(x.responseText);
									pageOrchestrator.goToAlbumPage(pageContent.creator, pageContent.name);
									break;
							}
					}
			});
		};
		let nextPageButtonListenerShape = (e) => {
			e.preventDefault();
			start=start+5;
			imagesContainer.update(pageContent.images, start, start+5, pageContent.name);//tra next e previous c'è solo la differenza di un "+-5"
			showNavigateButtons();
		}
		let previousPageButtonListenerShape = (e) => {
			e.preventDefault();
			start=start-5;
			imagesContainer.update(pageContent.images, start, start+5, pageContent.name);
			showNavigateButtons();
		}
		this.updateContent = function (pc, i) {
			if (pageContent!=null)
				pageContent.images[i] = pc;
		}
		this.addSortButtonListener = function(target, event="click", content) {
			target.removeEventListener(event, sortButtonListenerShape);
			pageContent = content;
			target.addEventListener(event, sortButtonListenerShape);
		}
		this.addDeleteButtonListener = function(target, event="click", content) {
			target.removeEventListener(event, deleteButtonListenerShape);
			pageContent = content;
			target.addEventListener(event, deleteButtonListenerShape);
		}
		this.addAlbumNavigationButtonsListeners = function(target_previous, event_previous, target_next, event_next, content, container) {
			start=0;
			pageContent = content;
			imagesContainer = container;
			previous_button = target_previous;
			next_button = target_next;
			previous_button.addEventListener(event_previous, previousPageButtonListenerShape);
			next_button.addEventListener(event_next, nextPageButtonListenerShape);
			showNavigateButtons();
		}
	}
	function CreateAlbumPageListenersDirector(pageOrchestrator) {
	
		let creatingAlbumCheck = function(form) {
			
			let error_message = "";
		
			if (form.title.value == null || form.title.value == "")
				{				
					error_message = "il nome dell'album e' un campo obbligatorio";
					pageOrchestrator.showErrorParagraph(error_message);
					return (false);
				}
			if (Array.from(form.title.value).length>50)
				{
					error_message = "la dimensione massima per il nome e' 50 caratteri";
					pageOrchestrator.showErrorParagraph(error_message);
					return (false);
				}
			
			let choices = form.elements;
			for (let i = 1; i < choices.length-1; i++)
				{
					if (choices[i].checked)
						{
							return (true);
						}
				}
				
			error_message = "nessuna immagine selezionata";
			pageOrchestrator.showErrorParagraph(error_message);
			return (false);
		};
	
		let createAlbumButtonListenerShape = (e) => {
			e.preventDefault();
			if (creatingAlbumCheck(e.target.closest("form")))
				{
					sendAsynRequest("POST", basic_url+'DoCreateAlbum', e.target.closest("form"), function(x) {
						if (x.readyState == XMLHttpRequest.DONE)
					    	{
					        	switch (x.status)
					         		{
										case 200:
											pageOrchestrator.backToHome();
											break;
										default:
											pageOrchestrator.showErrorParagraph(x.responseText);
											break;
					           		}
					       	}
					});
				}
		};
		
		this.addCreateAlbumButtonListener = function (target, event="click") {
			target.removeEventListener(event, createAlbumButtonListenerShape);
			target.addEventListener(event, createAlbumButtonListenerShape);
		}
	}
	function HomepageListenersDirector(pageOrchestrator) {
		let goToCreateAlbumButtonListenerShape = (e) => {
			e.preventDefault();
			pageOrchestrator.goToCreateAlbum();
		};
		let goToUploadImageButtonListenerShape = (e) => {
			e.preventDefault();
			pageOrchestrator.goToUploadImage();
		};
		let goToImageButtonListenerShape = (e, creator, name, returnAddress="") => {
			e.preventDefault();
			pageOrchestrator.goToImagePage(name, creator, returnAddress);
		};
		let goToAlbumButtonListenerShape = (e, creator, name) => {
			e.preventDefault();
			pageOrchestrator.goToAlbumPage(creator, name);
		};
		
		this.addAlbumButtonListener = function (target, event="click", creator, name) {
			target.addEventListener(event, (e) => {
				goToAlbumButtonListenerShape(e, creator, name);
			});
		};
		this.addImageButtonListener = function (target, event="click", name, creator, returnAddress="") {
			target.addEventListener(event, (e) => {
				goToImageButtonListenerShape(e, creator, name, returnAddress);
			});
		};
		this.addCreateAlbumButtonListener = function (target, event="click") {
			target.removeEventListener(event, goToCreateAlbumButtonListenerShape);
			target.addEventListener(event, goToCreateAlbumButtonListenerShape);
		};
		this.addUploadImageButtonListener = function (target, event="click") {
			target.removeEventListener(event, goToUploadImageButtonListenerShape);
			target.addEventListener(event, goToUploadImageButtonListenerShape);
		};
	}
	function ImagePageListenersDirector (pageOrchestrator) {	
		let pageContent;
		
		let checkingPostComment = function(form) {
			
			if (form.elements["text"].value == null || form.elements["text"].value == "")
				{
					pageOrchestrator.showErrorParagraph("non e' possibile inviare commenti vuoti");
					return (false);
				}
			if (Array.from(form.elements["text"].value).length>500)
				{
					pageOrchestrator.showErrorParagraph("la dimensione massima per un commento e' 500 caratteri");
					return (false);
				}
				
			return (true);
		}
		let deleteButtonListenerShape = (e) => {
			e.preventDefault();
			
			sendAsynRequest("POST", basic_url+'DoDeleteImage', e.target.closest("form"), function(x) {
				if (x.readyState == XMLHttpRequest.DONE)
					{
						switch (x.status)
							{
								case 200:
									pageOrchestrator.backToHome();
									break;
								default:
									pageOrchestrator.showErrorParagraph(x.responseText);
									pageOrchestrator.goToImagePage(pageContent.name, pageContent.creator);
									break;
							}
					}
			});
		};
		let uploadCommentButtonListenerShape = (e) => {
			e.preventDefault();
			let form = e.target.closest("form");
			if (checkingPostComment(form))
				{
					sendAsynRequest("POST", basic_url+'DoPostComment', form, (x) => {
						if (x.readyState == XMLHttpRequest.DONE)
					    	{
					        	switch (x.status)
					         		{
										case 200:
											pageOrchestrator.goToImagePage(form.elements["name"].value, form.elements["user"].value);
											break;
										default:
											pageOrchestrator.showErrorParagraph(x.responseText);
											pageOrchestrator.goToImagePage(form.elements["name"].value, form.elements["user"].value);
											break;
					           		}
					       	}
					});	
				}
		};
		
		this.addDeleteButtonListener = function(target, event="click", content) {
			target.removeEventListener(event, deleteButtonListenerShape);
			pageContent = content;
			target.addEventListener(event, deleteButtonListenerShape);
		}
		this.addUploadCommentButtonListener = function (target, event="click") {
			target.removeEventListener(event, uploadCommentButtonListenerShape);
			target.addEventListener(event, uploadCommentButtonListenerShape);
		};	
	}
	function MenubarListenersDirector(pageOrchestrator) {
		let goBack;
		
		let backButtonListenerShape = (e) => {
			e.preventDefault();
			goBack();
		};
		let logoutButtonListenerShape = (e)=>{
			e.preventDefault();
			pageOrchestrator.logout();
		};
		let homeButtonListenerShape = (e)=>{
			e.preventDefault();
			pageOrchestrator.backToHome();
		};
			
		this.addBackButtonListener = function(target, event="click", creator, name) {
			target.removeEventListener(event, backButtonListenerShape);
			goBack = () => {
				pageOrchestrator.goToAlbumPage(creator, name);
			};
			target.addEventListener(event, backButtonListenerShape);
		}
		this.addLogoutButtonListener = function (target, event="click") {
			target.removeEventListener(event, logoutButtonListenerShape);
			target.addEventListener(event, logoutButtonListenerShape);
		};
		this.addHomeButtonListener = function (target, event="click") {
			target.removeEventListener(event, homeButtonListenerShape);
			target.addEventListener(event, homeButtonListenerShape);
		};
	}
	function ModalboxListenersDirector (pageOrchestrator, modalBoxContent, index) {
		let pageContent;
		let image;
		
		let uploadCommentButtonListenerShape = (e) => {
			e.preventDefault();
			let form = e.target.closest("form");
			
			if (form.elements["text"].value == null || form.elements["text"].value == "")
				{
					pageOrchestrator.showErrorParagraph("non e' possibile inviare commenti vuoti");
				}
			else
				{
					sendAsynRequest("POST", basic_url+'DoPostComment', form, (x) => {
						if (x.readyState == XMLHttpRequest.DONE)
					    	{
					        	if(x.status!=200)
					        		{
										pageOrchestrator.showErrorParagraph(x.responseText);
					           		}
					           	else
					           		{
										sendAsynRequest("GET", basic_url+'ImagePage?user=' + encodeURIComponent(pageContent.creator) + '&name=' + encodeURIComponent(pageContent.name), null, (x) => {
							                if (x.readyState == XMLHttpRequest.DONE)
							                	{
													if (x.status == 200)
														{
															let pc = JSON.parse(x.responseText);
															this.updateModalBoxShowListener("mouseover",pc); //si aggiorna il contenuto salvato per la finestra modale 
															modalBoxContent.update(pc);//si visualizza la finestra modale col nuovo contenuto
															pageOrchestrator.updateAlbumPageContent(pc, index);//si modificano le informazioni salvate dall'album)
																//in teoria andrebbe invocato solo quando si utilizzano
																//finestre modali dalle miniature di un album (chiamarlo quando)
															   //la finestra modale considerata non è la miniatura di un album
															   //è del tutto inutile
														}
													else
														{
															pageOrchestrator.showErrorParagraph(x.responseText);
															pageOrchestrator.backToHome();	
														}
							            		}
	         							});										
									}
					       	}
					});	
				}
		};
		
		let modalBoxFadeListenerShape = (e) => {
			modalBoxContent.hide();
		};
		
		let modalBoxShowListenerShape = (e) => {
			modalBoxContent.setActualListener(index);
			modalBoxContent.update(pageContent);
		};
		
		this.addModalBoxFadeListener = function (target, event="mouseleave") {
			target.addEventListener(event, modalBoxFadeListenerShape);
		};
		
		this.addUploadCommentButtonListener = function (target, event="click") {
			target.addEventListener(event, uploadCommentButtonListenerShape);
		};
		
		this.addModalBoxShowListener = function (target, event="mouseover", pc) {
			target.removeEventListener(event, modalBoxShowListenerShape);
			image = target;
			pageContent = pc;
			target.addEventListener(event, modalBoxShowListenerShape);
		};
		this.removeUploadCommentButtonListener = function (target, event="click") {
			target.removeEventListener(event, uploadCommentButtonListenerShape);
		};
		this.removeModalBoxFadeListener = function (target, event="mouseleave") {
			target.removeEventListener(event, modalBoxFadeListenerShape);
		};
		this.updateModalBoxShowListener = function (event="mouseover", pc) {
			image.removeEventListener(event, modalBoxShowListenerShape);
			pageContent = pc;
			image.addEventListener(event, modalBoxShowListenerShape);	
		}
		this.kill = function (event1="mouseover", event2="click", event3="mouseleave", comment_button, box_area) {//quando si cambia "pagina", i listener
			image.removeEventListener(event1, modalBoxShowListenerShape);										  //di tutte le finestre modali vengono rimossi
			comment_button.removeEventListener(event2, uploadCommentButtonListenerShape);
			box_area.removeEventListener(event3, modalBoxFadeListenerShape);
		}
	}
	function SortAlbumPageListenersDirector (pageOrchestrator) {
		let dragged_row;
		let sortAlbumButtonListenerShape = (e) => {
			e.preventDefault();
			let form1 = e.target.closest("form");
			sendAsynRequest("POST", basic_url+'DoSortAlbum', form1,(x) => {
				if (x.readyState == XMLHttpRequest.DONE)
					{
				    	switch (x.status)
				        	{
								case 200:
									pageOrchestrator.goToAlbumPage(form1.elements["user"].value, form1.elements["name"].value);
									break;
								default:
									pageOrchestrator.showErrorParagraph(x.responseText);
									pageOrchestrator.goToSortAlbumPage(form1.elements["user"].value, form1.elements["name"].value);
									break;
				           	}
				    }
			});
		};
		let rowsDragStartListenerShape = (e) => { //quando si inizia a trascinare una riga il suo indice viene salvato in "dragged_row"
			dragged_row = e.target.getAttribute("index");
		};
		let rowsDragOverListenerShape = (e) => { //quando "si passa" con una riga sopra un'altra, le due vengono scambiate (e la "dragged_row"
												 //diventa la riga su cui si è appena "passati"
			let self = e.target.closest("tr");
			let children = self.closest("table").children;
			swapElements(children[self.getAttribute("index")].getElementsByTagName("img")[0], children[dragged_row].getElementsByTagName("img")[0]);
			swapElements(children[self.getAttribute("index")].getElementsByTagName("div")[0], children[dragged_row].getElementsByTagName("div")[0]);
			swapElements(children[self.getAttribute("index")].getElementsByTagName("input")[0], children[dragged_row].getElementsByTagName("input")[0]);
			dragged_row = self.getAttribute("index");
		};
		let swapElements = function(obj1, obj2) { //modo comodo per scambiare due elementi HTML senza ricorrere a "innerHTML"
												  //(che "cancellerebbe" tutti i listener)
			let temp = document.createElement("div");
		    obj1.parentNode.insertBefore(temp, obj1);
		    obj2.parentNode.insertBefore(obj1, obj2);
		    temp.parentNode.insertBefore(obj2, temp);
		    temp.parentNode.removeChild(temp);
		};
		this.addSortAlbumButtonListener = function (target, event) {
			target.removeEventListener(event, sortAlbumButtonListenerShape);
			target.addEventListener(event, sortAlbumButtonListenerShape);
		};
		this.addSwapRowListeners = function (target) {
			target.addEventListener("dragover", (e)=>{e.preventDefault();});
			target.addEventListener("dragenter", (e)=>{e.preventDefault();});
			target.addEventListener("dragstart", rowsDragStartListenerShape);
			target.addEventListener("dragover", rowsDragOverListenerShape);
		};
	}
	
	function ImagesCarousel(container, pageOrchestrator) {
		this.update = function (images_infos, start=0, end=images_infos.length, returnAddress="") {
			container.innerHTML = "";
			container.setAttribute("style", "display:table");
			let row_for_thumbnails = document.createElement("tr");
				container.appendChild(row_for_thumbnails);
			let row_for_names = document.createElement("tr");
				container.appendChild(row_for_names);
			
			let real_end = end>images_infos.length?images_infos.length:end;
			
			for (let i = start ; i < real_end ; i++)
				{
					let col_for_image = document.createElement("th");
						col_for_image.setAttribute("width", "20%");
					let col_for_name = document.createElement("th");
						col_for_name.setAttribute("width", "20%");
					
					//if (images_infos[i].elem1 !== null)
						//{
								row_for_thumbnails.appendChild(col_for_image);
								row_for_names.appendChild(col_for_name);
							let image = document.createElement("img");
								image.setAttribute("class", "thumbnail");
								image.setAttribute("src", images_infos[i].path);
								image.setAttribute("alt", images_infos[i].name);
								col_for_image.appendChild(image);
							let image_name = document.createElement("button");
								image_name.setAttribute("class", "small_generic_buttons");
								pageOrchestrator.homepageListenersDirector.addImageButtonListener(image_name, "click", images_infos[i].name, images_infos[i].creator, returnAddress);
								image_name.appendChild(document.createTextNode(images_infos[i].name));
								col_for_name.appendChild(image_name);
								pageOrchestrator.addModalBoxListener(image, images_infos[i], i); //il "set" delle informazioni per la finestra modale parte sempre da dei view objects
						//}
				}
			for (let i = real_end ; i < end ; i++)
				{
					let col_for_image = document.createElement("th");
						col_for_image.setAttribute("width", "20%");
					let col_for_name = document.createElement("th");
						col_for_name.setAttribute("width", "20%");
					row_for_thumbnails.appendChild(col_for_image);
					row_for_names.appendChild(col_for_name);
				}
		}
		this.hide = function () {
			container.setAttribute("style", "display:none");
		}
	}
	function AlbumsCarousel(container, belongs_to_me, pageOrchestrator) {
		this.update = function (album_infos) {
			container.innerHTML = "";
			container.setAttribute("style", "display:table");
			let row_for_names = document.createElement("tr");
				container.appendChild(row_for_names);
	
			for (let i = 0 ; i < album_infos.length ; i++)
				{
					let display_name = belongs_to_me?album_infos[i].elem1:album_infos[i].elem2+"/"+album_infos[i].elem1;
					let col_for_name = document.createElement("th");
						row_for_names.appendChild(col_for_name);
					let image_name = document.createElement("button");
						image_name.setAttribute("class", "small_generic_buttons");
						pageOrchestrator.homepageListenersDirector.addAlbumButtonListener(image_name, "click", album_infos[i].elem2, album_infos[i].elem1);
						image_name.appendChild(document.createTextNode(display_name));
						col_for_name.appendChild(image_name);	
				}
		}
		this.hide = function () {
			container.setAttribute("style", "display:none");
		}
	}
	function HomePageContent(pageOrchestrator) {	
		
		pageOrchestrator.homepageListenersDirector.addCreateAlbumButtonListener(document.getElementById("goToCreateAlbum_button"), "click");
		pageOrchestrator.homepageListenersDirector.addUploadImageButtonListener(document.getElementById("goToUploadImage_button"), "click");
		
		let imagesCarousel = new ImagesCarousel(document.getElementById("images_carousel"), pageOrchestrator);
		let userAlbumsCarousel = new AlbumsCarousel(document.getElementById("userAlbums_carousel"), true, pageOrchestrator);
		let nonUserAlbumsCarousel = new AlbumsCarousel(document.getElementById("nonUserAlbums_carousel"), false, pageOrchestrator);
	
		this.show = () => {
			sendAsynRequest("GET", basic_url+'Homepage', null, (x) => {
				if (x.readyState == XMLHttpRequest.DONE)
			        {
			            switch (x.status)
			            	{
								case 200:
									document.getElementById("homepage_body").setAttribute("style", "visibility:visible");
									this.update(JSON.parse(x.responseText));
									break;
								case 403:
									document.getElementById("homepage_body").setAttribute("style", "visibility:hidden");
									window.location.href = "register.html";
									break;
								default:
									document.getElementById("homepage_body").setAttribute("style", "visibility:visible");
									pageOrchestrator.showErrorParagraph(x.responseText);
									break;
			            	}
			         }
			});
		};
		this.update = function (homepage_content) {
			document.getElementById("home_page").setAttribute("style", "display:table");
			imagesCarousel.update(homepage_content.images_infos);
			userAlbumsCarousel.update(homepage_content.user_albums);
			nonUserAlbumsCarousel.update(homepage_content.nonUserAlbums);
		}
		this.hide = function () {
			document.getElementById("home_page").setAttribute("style", "display:none");
			imagesCarousel.hide();
			userAlbumsCarousel.hide();
			nonUserAlbumsCarousel.hide();
		}
	}
	function AlbumForm(pageOrchestrator) {
	
		let imagesCarousel = new ImagesCarousel(document.getElementById("album_page_carousel"), pageOrchestrator);
		
		this.show = (user, name) => {
			sendAsynRequest("GET", basic_url+'AlbumPage?user='+encodeURIComponent(user)+"&name="+encodeURIComponent(name), null, (x) => {
				if (x.readyState == XMLHttpRequest.DONE)
			        {
			            switch (x.status)
			            	{
								case 200:
									this.update(JSON.parse(x.responseText));
									break;
								default:
									pageOrchestrator.showErrorParagraph(x.responseText);
									pageOrchestrator.backToHome();
									break;
			            	}
			         }
			});
		};

		this.update = function (pageContent) {
			document.getElementById("album_page").setAttribute("style", "display:table");
			document.getElementById("album_name").innerHTML = "";
			document.getElementById("album_name").appendChild(document.createTextNode(pageContent.name));
			document.getElementById("album_date").innerHTML = "";
			document.getElementById("album_date").appendChild(document.createTextNode(pageContent.date + " / " + pageContent.creator));
			document.getElementById("delete_album_button_container").style.visibility="hidden";
			document.getElementById("sort_album_button_container").innerHTML="";
			document.getElementById("previous_button_container").innerHTML="";
			document.getElementById("next_button_container").innerHTML="";
			
			if (pageContent.creator == localStorage.getItem("username").trimEnd())
				{
					document.getElementById("delete_album_button_container").style.visibility="visible";
					document.getElementById("delete_album_button_form").elements["name"].value = pageContent.name;
					let delete_album_button = document.getElementById("delete_album_button");
					pageOrchestrator.albumPageListenersDirector.addDeleteButtonListener(delete_album_button, "click", pageContent);
				}
			let next_carousel_button = document.createElement("button");
				document.getElementById("next_button_container").appendChild(next_carousel_button);
				next_carousel_button.setAttribute("class", "generic_buttons");
				next_carousel_button.setAttribute("id", "next_carousel_button");
				let next_icon = document.createElement("i");
					next_icon.setAttribute("class", "fa fa-arrow-right");
					next_icon.setAttribute("aria-hidden", "true");
					next_carousel_button.appendChild(document.createTextNode("SUCCESSIVE "));
					next_carousel_button.appendChild(next_icon);
			let previous_carousel_button = document.createElement("button");
				document.getElementById("previous_button_container").appendChild(previous_carousel_button);
				previous_carousel_button.setAttribute("class", "generic_buttons");
				previous_carousel_button.setAttribute("id", "previous_carousel_button");
				let previous_icon = document.createElement("i");
					previous_icon.setAttribute("class", "fa fa-arrow-left");
					previous_icon.setAttribute("aria-hidden", "true");
					previous_carousel_button.appendChild(previous_icon);
				previous_carousel_button.appendChild(document.createTextNode(" PRECEDENTI"));
		
			let sort_album_button = document.createElement("button");
				document.getElementById("sort_album_button_container").appendChild(sort_album_button);
				sort_album_button.setAttribute("class", "generic_buttons");
				let sort_icon = document.createElement("i");
					sort_icon.setAttribute("class", "fa fa-sort");
					sort_icon.setAttribute("aria-hidden", "true");
					sort_album_button.appendChild(sort_icon);
				sort_album_button.appendChild(document.createTextNode(" Ordina album"));
					
			pageOrchestrator.albumPageListenersDirector.addSortButtonListener(sort_album_button, "click", pageContent);
			pageOrchestrator.albumPageListenersDirector.addAlbumNavigationButtonsListeners(
				document.getElementById("previous_carousel_button"),"click",
				document.getElementById("next_carousel_button"),"click",
				pageContent, imagesCarousel
			);
			imagesCarousel.update(pageContent.images, 0, 5, pageContent.name);
		};
		this.hide = function () {
			document.getElementById("album_page").setAttribute("style", "display:none");
		};	
	}
	function UploadImageForm(pageOrchestrator) {
		
		let upload_button = document.getElementById("upload_image_button");
			pageOrchestrator.uploadImagePageListenersDirector.addUploadImageButtonListener(upload_button, "click");
	
		this.update = function () {
			document.getElementById("upload_image").setAttribute("style", "display:table");
		};
		this.hide = function () {
			document.getElementById("upload_image").setAttribute("style", "display:none");
		};
	}
	function CreateAlbumForm(pageOrchestrator) {
		let createAlbumButton = document.getElementById("create_album_button");
			pageOrchestrator.createAlbumPageListenersDirector.addCreateAlbumButtonListener(createAlbumButton, "click");
		
		let insider_container = document.getElementById("images_list");
		let container = document.getElementById("create_album_scroll_div");
		this.show = () => {
			sendAsynRequest("GET", basic_url+'CreateAlbum', null, (x)=> {
				if (x.readyState == XMLHttpRequest.DONE)
			        {
			            switch (x.status)
			            	{
								case 200:
									this.update(JSON.parse(x.responseText));
									break;
								default:
									pageOrchestrator.showErrorParagraph(x.responseText);
									pageOrchestrator.backToHome();
									break;
			            	}
			         }
			});
		};
		this.update = function (pageContent) {
			document.getElementById("create_album").setAttribute("style", "display:table");
			container.setAttribute("style", "height:"+pageContent.fixed_table_height);
			insider_container.setAttribute("style", "height:"+pageContent.fixed_table_height);
			for (let i = 0 ; i < pageContent.my_images.length ; i++)
				{
					let image_row = document.createElement("tr");
						insider_container.appendChild(image_row);
					let image_col = document.createElement("th");
						image_row.appendChild(image_col);
						let image = document.createElement("img");
							image.setAttribute("class", "thumbnail");
							image.setAttribute("alt", pageContent.my_images[i].name);
							image.setAttribute("src", pageContent.my_images[i].path);
							image_col.appendChild(image);
					let name_col = document.createElement("th");
						image_row.appendChild(name_col);
						let image_name = document.createElement("div");
							image_name.setAttribute("class", "create_album_image_name");
							image_name.appendChild(document.createTextNode(pageContent.my_images[i].name));
							name_col.appendChild(image_name);
					let checkbox_col = document.createElement("th");
						image_row.appendChild(checkbox_col);
						let checkbox = document.createElement("input");
							checkbox.setAttribute("type", "checkbox");
							checkbox.setAttribute("name", "choices");
							checkbox.setAttribute("value", pageContent.my_images[i].name);
							checkbox.setAttribute("class", "create_album_checkboxes");
							checkbox_col.appendChild(checkbox);
					
					pageOrchestrator.addModalBoxListener(image, pageContent.my_images[i], i);
				}
		};
		this.hide = function () {
			document.getElementById("create_album").setAttribute("style", "display:none");
			document.getElementById("images_list").innerHTML="";
		};
	}
	function Menubar(pageOrchestrator) {	
		let logout_button = document.getElementById("logout_button");
			pageOrchestrator.menubarListenersDirector.addLogoutButtonListener(logout_button);
		
		let home_button = document.getElementById("home_button");
			pageOrchestrator.menubarListenersDirector.addHomeButtonListener(home_button);
			
		let back_button = document.getElementById("back_button");
			pageOrchestrator.menubarListenersDirector.addBackButtonListener(back_button);
	
		this.update = function () {
			document.getElementById("menubar").setAttribute("style", "display:table");
		}
		this.hideHomeButton = function () {
			home_button.setAttribute("style", "display:none");
		};
		this.showHomeButton = function () {
			home_button.setAttribute("style", "display:block");
		};
		this.hideLogoutButton = function () {
			logout_button.setAttribute("style", "display:none");
		};
		this.showLogoutButton = function () {
			logout_button.setAttribute("style", "display:block");
		};
		this.hideBackButton = function () {
			back_button.setAttribute("style", "display:none");
		};
		this.showBackButton = function () {
			back_button.setAttribute("style", "display:block");
		};
		this.configureBackButton = function (creator, name) {
			pageOrchestrator.menubarListenersDirector.addBackButtonListener(back_button, "click", creator, name);
		}
		this.hide = function () {
			document.getElementById("menubar").setAttribute("style", "display:none");
		};
	}
	function ImagePageContent(pageOrchestrator) {
		
		pageOrchestrator.imagePageListenersDirector.addUploadCommentButtonListener(document.getElementById("upload_comment_button"), "click");
	
		this.show = (creator, name) => {
			sendAsynRequest("GET", basic_url+'ImagePage?user='+encodeURIComponent(creator)+'&name='+encodeURIComponent(name), null, (x) => {
				if (x.readyState == XMLHttpRequest.DONE)
			        {
			            switch (x.status)
			            	{
								case 200:
									this.update(JSON.parse(x.responseText));
									break;
								default:
									pageOrchestrator.showErrorParagraph(x.responseText);
									pageOrchestrator.backToHome();
									break;
			            	}
			        }
			});
		}
		this.update = function (pageContent) {
			document.getElementById("image_page").setAttribute("style", "display:table");
			document.getElementById("image_title").innerHTML="";
			document.getElementById("image_title").appendChild(document.createTextNode(pageContent.name));
			document.getElementById("image_desc").innerHTML="";
			document.getElementById("image_desc").appendChild(document.createTextNode(pageContent.text));
			document.getElementById("image_date").innerHTML="";
			document.getElementById("image_date").appendChild(document.createTextNode(pageContent.date));
			document.getElementById("image_author").innerHTML="";
			document.getElementById("delete_image_button_container").style.visibility="hidden";
			document.getElementById("image_author").appendChild(document.createTextNode(pageContent.creator));
			let fixed_comment_box_height = pageContent.comments.length>2?200:100*pageContent.comments.length;
			document.getElementById("comment_box_div").setAttribute("style", "height:"+fixed_comment_box_height+"px");
			document.getElementById("real_image").innerHTML="";
			document.getElementById("real_image").setAttribute("src", pageContent.path);
			document.getElementById("real_image").setAttribute("alt", pageContent.name);
			document.getElementById("comment_box_table").innerHTML="";
			document.getElementById("upload_comment_button").closest("form").getElementsByTagName("input")[0].setAttribute("value", pageContent.creator);
			document.getElementById("upload_comment_button").closest("form").getElementsByTagName("input")[1].setAttribute("value", pageContent.name);
			
			if (pageContent.creator == localStorage.getItem("username").trimEnd())
				{
					document.getElementById("delete_image_button_container").style.visibility="visible";
					let delete_image_form = document.getElementById("delete_image_form");
					let delete_image_button = document.getElementById("delete_image_button");
					delete_image_form.elements["name"].value = pageContent.name;
					pageOrchestrator.imagePageListenersDirector.addDeleteButtonListener(delete_image_button, "click", pageContent);
				}
			
			
				for (let i = 0 ; i < pageContent.comments.length ; i++)
					{
						let content_row = document.createElement("tr");
							document.getElementById("comment_box_table").appendChild(content_row);
						let content_col = document.createElement("th");
							content_row.appendChild(content_col);
						let internal_form_table = document.createElement("table");
							internal_form_table.setAttribute("class", "internal_form_table");
							content_col.appendChild(internal_form_table);
							let table_row_1 = document.createElement("tr");
								internal_form_table.appendChild(table_row_1);
								let table_col_1 = document.createElement("th");
									table_col_1.setAttribute("colspan", "2");
									table_row_1.appendChild(table_col_1);
									let comment_area = document.createElement("textarea");
										comment_area.appendChild(document.createTextNode(pageContent.comments[i].text));
										comment_area.setAttribute("cols", "52");
										comment_area.setAttribute("class", "comment_textarea");
										comment_area.readOnly = true;
										table_col_1.appendChild(comment_area);
							let table_row_2 = document.createElement("tr");
								internal_form_table.appendChild(table_row_2);
								let table_row_2_col_1 = document.createElement("th");
									table_row_2_col_1.setAttribute("align", "left");
									table_row_2_col_1.appendChild(document.createTextNode(pageContent.comments[i].author));
									table_row_2.appendChild(table_row_2_col_1);
								let table_row_2_col_2 = document.createElement("th");
									table_row_2_col_2.setAttribute("align", "right");
									table_row_2_col_2.appendChild(document.createTextNode(pageContent.comments[i].timestamp));
									table_row_2.appendChild(table_row_2_col_2);
							let table_row_3 = document.createElement("tr");
								internal_form_table.appendChild(table_row_3);
								let table_row_3_col_1 = document.createElement("th");
									table_row_3.appendChild(table_row_3_col_1);
									table_row_3_col_1.setAttribute("colspan", "2");
									table_row_3_col_1.appendChild(document.createElement("hr"));
					}
		}
		this.hide = function () {
			document.getElementById("image_page").setAttribute("style", "display:none");
		};
	}
	function ModalBoxContent(pageOrchestrator) {
		let listeners = [];
		let actualListener;
		
		this.setActualListener = function (index) {
			if (listeners[actualListener] != null)
				{
					listeners[actualListener].removeUploadCommentButtonListener(document.getElementById("mb_upload_comment_button"),"click");
					listeners[actualListener].removeModalBoxFadeListener(document.getElementById("modal_box_content"),"mouseleave");
				}
				
			actualListener = index;
			listeners[actualListener].addModalBoxFadeListener(document.getElementById("modal_box_content"),"mouseleave");
			listeners[actualListener].addUploadCommentButtonListener(document.getElementById("mb_upload_comment_button"),"click");
		};
		this.addListeners = function (target, pageContent, i) {//si aggiungono le informazioni per le finestre modali
			if (listeners[i] == null)
				{
					listeners[i] = new ModalboxListenersDirector(pageOrchestrator, this, listeners.length); //unici directors non presenti nel pagOrchestrator
				}
				
			listeners[i].addModalBoxShowListener(target,"mouseover",pageContent);	
		};
		this.clearListeners = function () {
			for (let i = 0 ; i < listeners.length ; i++)
				{
					listeners[i].kill("mouseover", "click", "mouseleave", document.getElementById("mb_upload_comment_button"), document.getElementById("modal_box_content"));
				}
				
			listeners = [];
		};
		
		this.update = function (pageContent) {
			document.getElementById("modal_box_content").setAttribute("style", "display:table");
			document.getElementById("mb_image_title").innerHTML="";
			document.getElementById("mb_image_title").appendChild(document.createTextNode(pageContent.name));
			document.getElementById("mb_image_desc").innerHTML="";
			document.getElementById("mb_image_desc").appendChild(document.createTextNode(pageContent.text));
			document.getElementById("mb_image_date").innerHTML="";
			document.getElementById("mb_image_date").appendChild(document.createTextNode(pageContent.date));
			document.getElementById("mb_image_author").innerHTML="";
			document.getElementById("mb_image_author").appendChild(document.createTextNode(pageContent.creator));
			let fixed_comment_box_height = pageContent.comments.length>2?200:pageContent.comments.length*100;
			document.getElementById("mb_comment_box_div").setAttribute("style", "height:"+fixed_comment_box_height+"px");
			document.getElementById("mb_real_image").innerHTML="";
			document.getElementById("mb_real_image").setAttribute("src", pageContent.path);
			document.getElementById("mb_real_image").setAttribute("alt", pageContent.name);
			document.getElementById("mb_comment_box_table").innerHTML="";
			document.getElementById("mb_upload_comment_button").closest("form").getElementsByTagName("input")[0].setAttribute("value", pageContent.creator);
			document.getElementById("mb_upload_comment_button").closest("form").getElementsByTagName("input")[1].setAttribute("value", pageContent.name);
			
				for (let i = 0 ; i < pageContent.comments.length ; i++)
					{
						let content_row = document.createElement("tr");
							document.getElementById("mb_comment_box_table").appendChild(content_row);
						let content_col = document.createElement("th");
							content_row.appendChild(content_col);
						let internal_form_table = document.createElement("table");
							internal_form_table.setAttribute("class", "internal_form_table");
							content_col.appendChild(internal_form_table);
							let table_row_1 = document.createElement("tr");
								internal_form_table.appendChild(table_row_1);
								let table_col_1 = document.createElement("th");
									table_col_1.setAttribute("colspan", "2");
									table_row_1.appendChild(table_col_1);
									let comment_area = document.createElement("textarea");
										comment_area.appendChild(document.createTextNode(pageContent.comments[i].text));
										comment_area.setAttribute("rows", "4");
										comment_area.setAttribute("cols", "52");
										comment_area.setAttribute("class", "comment_textarea");
										comment_area.readOnly = true;
										table_col_1.appendChild(comment_area);
							let table_row_2 = document.createElement("tr");
								internal_form_table.appendChild(table_row_2);
								let table_row_2_col_1 = document.createElement("th");
									table_row_2_col_1.setAttribute("align", "left");
									table_row_2_col_1.appendChild(document.createTextNode(pageContent.comments[i].author));
									table_row_2.appendChild(table_row_2_col_1);
								let table_row_2_col_2 = document.createElement("th");
									table_row_2_col_2.setAttribute("align", "right");
									table_row_2_col_2.appendChild(document.createTextNode(pageContent.comments[i].timestamp));
									table_row_2.appendChild(table_row_2_col_2);
							let table_row_3 = document.createElement("tr");
								internal_form_table.appendChild(table_row_3);
								let table_row_3_col_1 = document.createElement("th");
									table_row_3.appendChild(table_row_3_col_1);
									table_row_3_col_1.setAttribute("colspan", "2");
									table_row_3_col_1.appendChild(document.createElement("hr"));
					}
		}
		this.hide = function () {
			document.getElementById("modal_box_content").setAttribute("style", "display:none");
		};
	}
	function SortAlbumForm(pageOrchestrator) {	
		pageOrchestrator.sortAlbumPageListenersDirector.addSortAlbumButtonListener(document.getElementById("sort_album_button"), "click");
		
		let page_container = document.getElementById("sort_album_page");
		let container = document.getElementById("sort_album_table");
		let div_container = document.getElementById("sort_album_div");
		
		this.show = (user, name) => {
			sendAsynRequest("GET", basic_url+'SortAlbumPage?user='+encodeURIComponent(user)+"&name="+encodeURIComponent(name), null, (x) => {
				if (x.readyState == XMLHttpRequest.DONE)
			        {
			            switch (x.status)
			            	{
								case 200:
									this.update(JSON.parse(x.responseText));
									break;
								default:
									pageOrchestrator.showErrorParagraph(x.responseText);
									pageOrchestrator.backToHome();
									break;
			            	}
			         }
			});
		};
		
		this.update = (pageContent) => {
			page_container.setAttribute("style", "display:table");
			container.innerHTML="";
			div_container.closest("form").getElementsByTagName("input")[0].setAttribute("value", pageContent.creator);
			div_container.closest("form").getElementsByTagName("input")[1].setAttribute("value", pageContent.name);
			let div_height = pageContent.images.length<5?61*pageContent.images.length:320;
			div_container.setAttribute("style", "height:"+div_height+"px");
			
			for (let i = 0 ; i < pageContent.images.length ; i++)
				{
					let image_row = document.createElement("tr");
						image_row.setAttribute("index", i);
						image_row.draggable = true;
						container.appendChild(image_row);
						let hidden = document.createElement("input");
							hidden.setAttribute("type", "hidden");
							hidden.setAttribute("name", "sorting");
							hidden.setAttribute("value",pageContent.images[i].name);
							image_row.appendChild(hidden);
						let col1 = document.createElement("th");
							image_row.appendChild(col1);
							let image = document.createElement("img");
								col1.appendChild(image);
								image.setAttribute("class", "small_thumbnail");
								image.setAttribute("alt", pageContent.images[i].name);
								image.setAttribute("src", pageContent.images[i].path);
						let col2 = document.createElement("th");
							image_row.appendChild(col2);
							let image_name = document.createElement("div");
								image_name.setAttribute("class", "create_album_image_name");
								image_name.appendChild(document.createTextNode(pageContent.images[i].name));
								col2.appendChild(image_name);
						let col3 = document.createElement("th");
							image_row.appendChild(col3);
							col3.setAttribute("align", "left");
							let icon = document.createElement("i");
								icon.setAttribute("class", "fa fa-arrows-v");
								col3.appendChild(icon);
						
						pageOrchestrator.addModalBoxListener(image,pageContent.images[i], i);
						
						pageOrchestrator.sortAlbumPageListenersDirector.addSwapRowListeners(image_row);					
				}
		};
		this.hide = function () {
			page_container.setAttribute("style", "display:none");
		};
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