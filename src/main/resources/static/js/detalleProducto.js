document.addEventListener('DOMContentLoaded', function() {
           // Funcionalidad para el botón de realizar pedido
           document.querySelector('.btn-realizar-pedido').addEventListener('click', function() {
               alert('Redirigiendo al carrito de compras...\n\nProducto: PC Gamer Haku Red Ryzen 7 5700G\nPrecio: s/1,899');
           });
           
           // Funcionalidad para el zoom de imagen
           document.querySelector('.zoom-icon').addEventListener('click', function() {
               const img = document.querySelector('.product-main-image');
               
               // Crear modal para zoom
               const modal = document.createElement('div');
               modal.innerHTML = `
                   <div style="position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.9); z-index: 9999; display: flex; align-items: center; justify-content: center; cursor: pointer;">
                       <img src="${img.src}" style="max-width: 90%; max-height: 90%; object-fit: contain;">
                       <div style="position: absolute; top: 20px; right: 30px; color: white; font-size: 2rem; cursor: pointer;">&times;</div>
                   </div>
               `;
               
               document.body.appendChild(modal);
               
               // Cerrar modal al hacer clic
               modal.addEventListener('click', function() {
                   document.body.removeChild(modal);
               });
           });
           
           // Funcionalidad para las flechas de navegación
           document.querySelector('.arrow-left .nav-arrow').addEventListener('click', function() {
               alert('Navegando al producto anterior...');
           });
           
           document.querySelector('.arrow-right .nav-arrow').addEventListener('click', function() {
               alert('Navegando al producto siguiente...');
           });
           
           // Ocultar flechas en móviles
           function toggleArrows() {
               const arrows = document.querySelectorAll('.navigation-arrows');
               if (window.innerWidth <= 768) {
                   arrows.forEach(arrow => arrow.style.display = 'none');
               } else {
                   arrows.forEach(arrow => arrow.style.display = 'block');
               }
           }
           
           toggleArrows();
           window.addEventListener('resize', toggleArrows);
       });