/**
 * Logique Chessmate - Placement
 */

let appState = {
    piece: localStorage.getItem('chessPiece') || 'Dame',
    mode: localStorage.getItem('chessMode') || 'place',
    color: localStorage.getItem('chessColor') || 'true',
    isLoading: false
};

document.addEventListener("DOMContentLoaded", () => {
    updateVisuals();
    checkServerMessage();
});

function playSFX(type) {
    if (!type) return;
    const audio = document.getElementById(`sfx-${type}`);
    if (audio) {
        audio.currentTime = 0;
        audio.play().catch(e => {});
    }
}

async function selectPiece(btn) {
    if (appState.isLoading) return;

    let raw = btn.getAttribute('data-piece');
    appState.piece = raw.charAt(0).toUpperCase() + raw.slice(1);
    localStorage.setItem('chessPiece', appState.piece);

    updateVisuals();

    const csrf = document.querySelector('input[name="_csrf"]').value;
    const formData = new FormData();
    formData.append('_csrf', csrf);
    formData.append('type', appState.piece);

    appState.isLoading = true;
    try {
        const res = await fetch('/placement/selectPiece', { method: 'POST', body: formData });
        if (res.ok) await updatePageContent(res);
    } catch (e) {
        console.error("Erreur sélection", e);
    } finally {
        appState.isLoading = false;
        setModePlace(); // Force le passage en mode placement
    }
}

async function selectColor(btn) {
    if (appState.isLoading) return;

    const colorValue = btn.getAttribute('data-color');

    appState.color = colorValue;
    localStorage.setItem('chessColor', appState.color);

    updateVisuals();

    const csrf = document.querySelector('input[name="_csrf"]').value;
    const formData = new FormData();
    formData.append('_csrf', csrf);
    formData.append('isWhite', appState.color);

    appState.isLoading = true;
    try {
        const res = await fetch('/placement/setColor', { method: 'POST', body: formData });
        if (res.ok) await updatePageContent(res);
    } catch (e) {
        console.error("Erreur changement couleur", e);
    } finally {
        appState.isLoading = false;
    }
}

async function clickCase(caseElem) {
    if (appState.isLoading) return;

    const csrf = document.querySelector('input[name="_csrf"]').value;
    const formData = new FormData();
    formData.append('_csrf', csrf);
    formData.append('x', caseElem.dataset.x);
    formData.append('y', caseElem.dataset.y);

    // On utilise la pièce stockée dans l'état global
    formData.append('type', appState.mode === 'place' ? appState.piece : '');

    // Si on est en mode "placer", on envoie aussi la couleur
    if (appState.mode === 'place') {
        formData.append('isWhite', appState.color);
    }

    appState.isLoading = true;
    try {
        const res = await fetch('/placement/action', { method: 'POST', body: formData });
        if (res.ok) await updatePageContent(res);
    } catch (e) {
        playSFX('error');
        showToast("Erreur de communication", "error");
    } finally {
        appState.isLoading = false;
    }
}

/* ---------------------------------------------------------
   MODIFICATION MAJEURE : MISE A JOUR ET NOTIFICATIONS
   --------------------------------------------------------- */

async function updatePageContent(response) {
    const html = await response.text();
    const doc = new DOMParser().parseFromString(html, 'text/html');

    const newContent = doc.querySelector('.main-container');
    if (newContent) {
        document.querySelector('.main-container').innerHTML = newContent.innerHTML;
    }

    // Gestion du message serveur (Sons, effets)
    checkServerMessage();

    // Gestion de l'alerte Overlay (pour qu'elle disparaisse proprement)
    const alert = document.querySelector('.messages-overlay .alert');
    if (alert) {
        setTimeout(() => {
            alert.classList.remove('animate__zoomIn');
            alert.classList.add('animate__fadeOutUp');

            setTimeout(() => {
                if(alert) alert.remove();
            }, 800);
        }, 4000);
    }

    updateVisuals();
}

function checkServerMessage() {
    const data = document.getElementById('server-data-message');

    if (data && data.dataset.msg && data.dataset.msg.trim() !== "") {
        const type = data.dataset.type;
        const msg = data.dataset.msg;
        const sound = data.dataset.sound;

        playSFX(sound);

        const overlayAlert = document.querySelector('.messages-overlay .alert');

        if (overlayAlert) {

            if (type === 'error') {
                shakeBoardEffect();
            }
            if (type === 'victory' && typeof lancerConfettis === "function") {
                lancerConfettis();
            }
        }
        else {

            if (type === 'error') {
                shakeBoardEffect();
                showToast(msg, 'error');
            } else {
                showToast(msg, type);
            }
        }

        data.dataset.msg = "";
    }
}

function showToast(m, t) {
    // Création dynamique du conteneur si absent
    let container = document.getElementById('notification-area');
    if (!container) {
        container = document.createElement('div');
        container.id = 'notification-area';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    // Mapping des types pour le CSS
    let cssType = t;
    let icon = 'fa-info-circle';

    if(t === 'error' || t === 'danger') { cssType = 'error'; icon = 'fa-exclamation-triangle'; }
    if(t === 'victory' || t === 'success') { cssType = 'success'; icon = 'fa-trophy'; }

    toast.className = `floating-notif ${cssType}`;
    toast.innerHTML = `<i class="fa ${icon}"></i> <span>${m}</span>`;

    container.appendChild(toast);

    // Auto-remove après 3.5s
    setTimeout(() => {
        toast.style.animation = 'slideUpFadeOut 0.4s forwards';
        setTimeout(() => toast.remove(), 400);
    }, 3500);
}

function shakeBoardEffect() {
    const board = document.getElementById('board');
    if (board) {
        board.classList.remove('shake-board');
        void board.offsetWidth; // Force Reflow
        board.classList.add('shake-board');
    }
}

/* ---------------------------------------------------------
   FIN DES MODIFICATIONS VISUELLES
   --------------------------------------------------------- */

function changeMode(select) {
    const mode = select.value;
    const customPanel = document.getElementById('customConfigPanel');

    if (mode === 'custom') {
        customPanel.style.display = 'block';
    } else {
        customPanel.style.display = 'none';

        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/placement/changeMode';

        const inputMode = document.createElement('input');
        inputMode.type = 'hidden';
        inputMode.name = 'modeDeJeu';
        inputMode.value = mode;
        form.appendChild(inputMode);

        const csrfToken = document.querySelector('input[name="_csrf"]').value;
        const inputCsrf = document.createElement('input');
        inputCsrf.type = 'hidden';
        inputCsrf.name = '_csrf';
        inputCsrf.value = csrfToken;
        form.appendChild(inputCsrf);

        document.body.appendChild(form);
        form.submit();
    }
}

function setModePlace() { appState.mode = 'place'; localStorage.setItem('chessMode', 'place'); updateVisuals(); }
function setModeRemove() { appState.mode = 'remove'; localStorage.setItem('chessMode', 'remove'); updateVisuals(); }

function updateVisuals() {
    document.querySelectorAll('.piece-btn').forEach(b => {
        if(b.getAttribute('data-piece').toLowerCase() === appState.piece.toLowerCase()) b.classList.add('selected');
        else b.classList.remove('selected');
    });
    const p = document.getElementById('modePlace'), r = document.getElementById('modeRemove');
    if(p && r) {
        if(appState.mode === 'place') { p.classList.add('selected'); r.classList.remove('selected'); }
        else { r.classList.add('selected'); p.classList.remove('selected'); }
    }

    document.querySelectorAll('.color-btn').forEach(b => {
        if (b.getAttribute('data-color') === String(appState.color)) {
            b.classList.add('selected');
        } else {
            b.classList.remove('selected');
        }
    });
}

async function resetBoard() {
    if (appState.isLoading) return;

    const csrf = document.querySelector('input[name="_csrf"]').value;
    const formData = new FormData();
    formData.append('_csrf', csrf);

    appState.isLoading = true;
    try {
        const res = await fetch('/placement/reset', { method: 'POST', body: formData });
        if (res.ok) {
            await updatePageContent(res);
            playSFX('remove');
        }
    } catch (e) {
        console.error("Erreur reset", e);
    } finally {
        appState.isLoading = false;
    }
}

document.addEventListener('DOMContentLoaded', function() {
    // Gestion du flash type au chargement initial (si pas AJAX)
    // Note: Dans le nouveau système, c'est checkServerMessage qui gère ça,
    // mais on garde ceci pour compatibilité si le Controller renvoie des attributs flash session.
    // Le checkServerMessage() appelé en haut suffit généralement.
});