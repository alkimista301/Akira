document.addEventListener('DOMContentLoaded', function() {
            const form = document.getElementById('contactForm');
            const textarea = document.querySelector('.comentarios-textarea');
            const charCounter = document.querySelector('.char-counter');
            
            // Contador de caracteres en tiempo real
            textarea.addEventListener('input', function() {
                const currentLength = this.value.length;
                charCounter.textContent = `(Máximo caracteres 1020)`;
                
                if (currentLength > 1000) {
                    charCounter.style.color = '#e74c3c';
                } else if (currentLength > 800) {
                    charCounter.style.color = '#f39c12';
                } else {
                    charCounter.style.color = '#007bff';
                }
            });
            
            // Validación del formulario
            form.addEventListener('submit', function(e) {
                e.preventDefault();
                
                const requiredFields = form.querySelectorAll('[required]');
                let isValid = true;
                
                requiredFields.forEach(field => {
                    if (!field.value.trim()) {
                        field.style.borderColor = '#e74c3c';
                        isValid = false;
                    } else {
                        field.style.borderColor = '#28a745';
                    }
                });
                
                // Validación específica de email
                const emailField = form.querySelector('[name="email"]');
                const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                if (emailField.value && !emailRegex.test(emailField.value)) {
                    emailField.style.borderColor = '#e74c3c';
                    isValid = false;
                }
                
                // Validación específica de DNI
                const dniField = form.querySelector('[name="dni"]');
                if (dniField.value && (dniField.value.length !== 8 || !/^\d+$/.test(dniField.value))) {
                    dniField.style.borderColor = '#e74c3c';
                    isValid = false;
                }
                
                if (isValid) {
                    alert('¡Formulario enviado correctamente! Nos contactaremos contigo en un plazo de 24 horas hábiles.');
                    form.reset();
                    // Aquí puedes agregar la lógica para enviar el formulario al servidor
                } else {
                    alert('Por favor, completa todos los campos requeridos correctamente.');
                }
            });
            
            // Limpiar estilos de error cuando el usuario empiece a escribir
            const inputs = form.querySelectorAll('input, textarea');
            inputs.forEach(input => {
                input.addEventListener('input', function() {
                    if (this.style.borderColor === 'rgb(231, 76, 60)') {
                        this.style.borderColor = '#ddd';
                    }
                });
            });
        });