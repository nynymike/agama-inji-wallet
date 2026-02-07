[#ftl output_format="HTML"]
<!DOCTYPE html>
<html lang="en">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"><title>Complete Your Profile</title></head>
<body>


    
    
    
    


    <div class="container">
        <h1>Complete Your Registration</h1>
        <p class="subtitle">Review your verified NID information and set a password</p>
        
        <div class="info-box">
            <p>Your information has been verified from your National ID credential. You can edit the fields if needed before completing registration.</p>
        </div>

        <button type="button" class="btn btn-secondary" id="toggleEditBtn" onclick="toggleEditMode()">
            <span id="editBtnText">✏️ Edit Information</span>
        </button>

        <form id="setupForm" method="post">
            <div class="form-group view-mode">
                <label for="displayName">Full Name</label>
                <input type="text" id="displayName" name="displayName" value="${vcUserInfo.displayName!''}" readonly="">
                <span class="edit-icon" onclick="enableField('displayName')">✏️</span>
            </div>

            <div class="form-group view-mode">
                <label for="mail">Email</label>
                <input type="email" id="mail" name="mail" value="${vcUserInfo.mail!''}" readonly="">
                <span class="edit-icon" onclick="enableField('mail')">✏️</span>
            </div>

            &lt;#if vcUserInfo.mobile?? &amp;&amp; vcUserInfo.mobile != ''&gt;
            <div class="form-group view-mode">
                <label for="mobile">Phone Number</label>
                <input type="tel" id="mobile" name="mobile" value="${vcUserInfo.mobile}" readonly="">
                <span class="edit-icon" onclick="enableField('mobile')">✏️</span>
            </div>
            <!--#if-->

            &lt;#if vcUserInfo.gender?? &amp;&amp; vcUserInfo.gender != ''&gt;
            <div class="form-group view-mode">
                <label for="gender">Gender</label>
                <input type="text" id="gender" name="gender" value="${vcUserInfo.gender}" readonly="">
                <span class="edit-icon" onclick="enableField('gender')">✏️</span>
            </div>
            <!--#if-->

            &lt;#if vcUserInfo.birthdate?? &amp;&amp; vcUserInfo.birthdate != ''&gt;
            <div class="form-group view-mode">
                <label for="birthdate">Date of Birth</label>
                <input type="text" id="birthdate" name="birthdate" value="${vcUserInfo.birthdate}" readonly="">
                <span class="edit-icon" onclick="enableField('birthdate')">✏️</span>
            </div>
            <!--#if-->

            <hr style="margin: 30px 0; border: none; border-top: 1px solid #eee;">

            <div class="form-group">
                <label for="password">Password <span class="required">*</span></label>
                <input type="password" id="password" name="password" required="">
                <p class="password-requirements">Minimum 8 characters, include uppercase, lowercase, and number</p>
                <p class="error" id="passwordError">Password does not meet requirements</p>
            </div>

            <div class="form-group">
                <label for="confirmPassword">Confirm Password <span class="required">*</span></label>
                <input type="password" id="confirmPassword" name="confirmPassword" required="">
                <p class="error" id="confirmError">Passwords do not match</p>
            </div>

            <button type="submit" class="btn">Complete Setup</button>
        </form>
    </div>

    

</body>
<style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif; background: #f5f5f5; padding: 20px; }
        .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); padding: 40px; }
        h1 { color: #333; margin-bottom: 10px; font-size: 24px; }
        .subtitle { color: #666; margin-bottom: 30px; font-size: 14px; }
        .form-group { margin-bottom: 20px; position: relative; }
        label { display: block; margin-bottom: 5px; color: #333; font-weight: 500; font-size: 14px; }
        input { width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; }
        input:focus { outline: none; border-color: #4CAF50; }
        input[readonly] { background-color: #f5f5f5; color: #666; cursor: not-allowed; }
        input.editable { background-color: #fff; color: #333; cursor: text; border-color: #4CAF50; }
        .required { color: #e74c3c; }
        .password-requirements { font-size: 12px; color: #666; margin-top: 5px; }
        .btn { width: 100%; padding: 14px; background: #4CAF50; color: white; border: none; border-radius: 4px; font-size: 16px; font-weight: 500; cursor: pointer; margin-top: 10px; }
        .btn:hover { background: #45a049; }
        .btn-secondary { background: #2196F3; margin-bottom: 10px; }
        .btn-secondary:hover { background: #1976D2; }
        .error { color: #e74c3c; font-size: 13px; margin-top: 5px; display: none; }
        .info-box { background: #e3f2fd; border-left: 4px solid #2196F3; padding: 15px; margin-bottom: 25px; border-radius: 4px; }
        .info-box p { color: #1976D2; font-size: 14px; }
        .edit-icon { position: absolute; right: 10px; top: 35px; color: #2196F3; cursor: pointer; font-size: 18px; display: none; }
        .edit-icon:hover { color: #1976D2; }
        .edit-mode .edit-icon { display: none; }
        .view-mode .edit-icon { display: block; }
    </style>
<script>
        const form = document.getElementById('setupForm');
        const password = document.getElementById('password');
        const confirmPassword = document.getElementById('confirmPassword');
        const passwordError = document.getElementById('passwordError');
        const confirmError = document.getElementById('confirmError');
        let editMode = false;

        function validatePassword(pwd) {
            return pwd.length >= 8 && 
                   /[A-Z]/.test(pwd) && 
                   /[a-z]/.test(pwd) && 
                   /[0-9]/.test(pwd);
        }

        function toggleEditMode() {
            editMode = !editMode;
            const editBtn = document.getElementById('editBtnText');
            const formGroups = document.querySelectorAll('.form-group.view-mode');
            
            if (editMode) {
                editBtn.textContent = '👁️ View Mode';
                formGroups.forEach(group => {
                    const input = group.querySelector('input');
                    if (input && input.id !== 'password' && input.id !== 'confirmPassword') {
                        input.removeAttribute('readonly');
                        input.classList.add('editable');
                    }
                    group.classList.remove('view-mode');
                    group.classList.add('edit-mode');
                });
            } else {
                editBtn.textContent = '✏️ Edit Information';
                formGroups.forEach(group => {
                    const input = group.querySelector('input');
                    if (input && input.id !== 'password' && input.id !== 'confirmPassword') {
                        input.setAttribute('readonly', 'readonly');
                        input.classList.remove('editable');
                    }
                    group.classList.remove('edit-mode');
                    group.classList.add('view-mode');
                });
            }
        }

        function enableField(fieldId) {
            const input = document.getElementById(fieldId);
            if (input) {
                input.removeAttribute('readonly');
                input.classList.add('editable');
                input.focus();
            }
        }

        form.addEventListener('submit', function(e) {
            let valid = true;
            passwordError.style.display = 'none';
            confirmError.style.display = 'none';

            if (!validatePassword(password.value)) {
                passwordError.style.display = 'block';
                valid = false;
            }

            if (password.value !== confirmPassword.value) {
                confirmError.style.display = 'block';
                valid = false;
            }

            if (!valid) {
                e.preventDefault();
            }
        });
    </script>
</html>