const moveUrl = '/puzzle/move';
const compMoveUrl = '/puzzle/computer-move';
const resetUrl = '/puzzle/reset';
const changeModeUrl = '/puzzle/changeMode';
const clearUrl = '/puzzle/clear';
const hintUrl = '/puzzle/hint';

let startCase = null;

/* ---------------------------------------------------------
   FONCTIONS DE FEEDBACK
   --------------------------------------------------------- */
function playSFX(type) {
    if (!type) return;
    const audio = document.getElementById(`sfx-${type}`);
    if (audio) {
        audio.currentTime = 0;
        audio.play().catch(e => {});
    }
}

function showToast(message, type = 'info') {
    let container = document.getElementById('notification-area');
    if (!container) {
        container = document.createElement('div');
        container.id = 'notification-area';
        document.body.appendChild(container);
    }

    let cssType = type;
    let icon = 'fa-info-circle';
    if(type === 'error' || type === 'danger') { cssType = 'error'; icon = 'fa-exclamation-triangle'; }
    if(type === 'victory' || type === 'success') { cssType = 'success'; icon = 'fa-trophy'; }

    const toast = document.createElement('div');
    toast.className = `floating-notif ${cssType}`;
    toast.innerHTML = `<i class="fa ${icon}"></i> <span>${message}</span>`;

    container.appendChild(toast);
    setTimeout(() => {
        toast.style.animation = 'slideUpFadeOut 0.4s forwards';
        setTimeout(() => toast.remove(), 400);
    }, 3500);
}

function shakeBoardEffect() {
    const board = document.getElementById('board');
    if (board) {
        board.classList.remove('shake-board');
        void board.offsetWidth;
        board.classList.add('shake-board');
    }
}

function checkServerMessage() {
    const data = document.getElementById('server-data-message');
    if (data && data.dataset.msg && data.dataset.msg.trim() !== "") {
        const type = data.dataset.type;
        const msg = data.dataset.msg;
        const sound = data.dataset.sound;

        playSFX(sound);

        const overlayAlert = document.querySelector('.messages-overlay .alert');

        // Si une grosse alerte est déjà au milieu de l'écran, on ne met PAS de toast
        if (overlayAlert) {
            if (type === 'error') shakeBoardEffect();
        } else {
            // Sinon, on met le toast (ex: erreurs réseau, infos discrètes)
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

/* ---------------------------------------------------------
   LOGIQUE DE MISE A JOUR
   --------------------------------------------------------- */

async function refreshGameState() {
    const loader = document.getElementById('board-loader');
    try {
        const response = await fetch('/puzzle', { method: 'GET' });
        if (response.ok) {
            await updatePageContent(response);
        } else {
            window.location.reload();
        }
    } catch (e) {
        console.error("Erreur refresh", e);
    } finally {
        if (loader) loader.style.display = 'none';
    }
}

async function updatePageContent(response) {
    const html = await response.text();
    const doc = new DOMParser().parseFromString(html, 'text/html');

    const newContent = doc.querySelector('.main-container');
    if (newContent) {
        document.querySelector('.main-container').innerHTML = newContent.innerHTML;
    }

    const alert = document.querySelector('.messages-overlay .alert');
    if (alert) {
        setTimeout(() => {
            alert.classList.remove('animate__zoomIn');
            alert.classList.add('animate__fadeOutUp');
            setTimeout(() => { if(alert) alert.remove(); }, 800);
        }, 4000);
    }

    checkServerMessage();

    const gameState = document.getElementById('game-state-data');
    if (gameState && gameState.dataset.ordiJoue === "true") {
        const thinkingLoader = document.getElementById('thinking-loader');
        if (thinkingLoader) thinkingLoader.style.display = 'block';
        setTimeout(() => sendRequest(compMoveUrl), 500);
    }

    attachEventListeners();
}

async function sendRequest(url, fd = new FormData()) {
    const token = document.querySelector('input[name="_csrf"]')?.value;
    const loader = document.getElementById('board-loader');

    const isHeavy = url.includes('reset') || url.includes('changeMode') || url.includes('load-from-data');
    if (isHeavy && loader) loader.style.display = 'flex';

    try {
        const response = await fetch(url, {
            method: 'POST',
            body: fd,
            headers: { 'X-CSRF-TOKEN': token }
        });

        if (response.redirected && response.url.includes('login')) {
            window.location.href = "/login";
            return;
        }

        if (response.ok) {
            await refreshGameState();

            if (url.includes('move') && !url.includes('computer')) {
                const data = document.getElementById('server-data-message');
                if (!data || !data.dataset.msg) playSFX('place');
            }
        }
    } catch(e) {
        console.error(e);
        showToast("Erreur de connexion", "error");
    } finally {
        if (isHeavy && loader) loader.style.display = 'none';
    }
}

function triggerChangeMode(v) {
    const fd = new FormData();
    fd.append('difficulte', v);
    sendRequest(changeModeUrl, fd);
}

function triggerLocalMode() {
    const diffSelect = document.getElementById('diffSelect');
    const difficulty = diffSelect ? diffSelect.value : 'any';
    const fd = new FormData();
    fd.append('difficulte', difficulty);
    sendRequest(changeModeUrl, fd);
}

function triggerReset() {
    sendRequest(resetUrl);
}

function clickPuzzleCase(el) {
    if (document.getElementById('thinking-loader')?.style.display === 'block') return;

    document.querySelectorAll('.hint-anim').forEach(e => e.classList.remove('hint-anim'));
    const x = el.getAttribute('data-x');
    const y = el.getAttribute('data-y');
    const hasPiece = el.querySelector('.piece') !== null;

    if (!startCase) {
        if (hasPiece) {
            startCase = el;
            el.classList.add('selected');
            el.style.backgroundColor = "rgba(255, 215, 0, 0.4)";
        }
    } else {
        const dx = startCase.getAttribute('data-x');
        const dy = startCase.getAttribute('data-y');

        startCase.style.backgroundColor = "";
        startCase.classList.remove('selected');

        if (dx === x && dy === y) {
            startCase = null;
            return;
        }

        const fd = new FormData();
        fd.append('departX', dx);
        fd.append('departY', dy);
        fd.append('arriveeX', x);
        fd.append('arriveeY', y);

        startCase = null;
        sendRequest(moveUrl, fd);
    }
}

function triggerHint() { sendRequest(hintUrl); }
function triggerClear() { sendRequest(clearUrl); }

function attachEventListeners() {
    const diffSelect = document.getElementById('diffSelect');
    if (diffSelect) {
        const newSelect = diffSelect.cloneNode(true);
        diffSelect.parentNode.replaceChild(newSelect, diffSelect);
        newSelect.addEventListener('change', function() { triggerChangeMode(this.value); });
    }
    const bindBtn = (id, func) => {
        const btn = document.getElementById(id);
        if (btn) {
            const newBtn = btn.cloneNode(true);
            btn.parentNode.replaceChild(newBtn, btn);
            newBtn.addEventListener('click', func);
        }
    };
    bindBtn('resetBtn', triggerReset);
    bindBtn('hintBtn', triggerHint);
    bindBtn('clearBtn', triggerClear);
}

document.addEventListener('DOMContentLoaded', function() {
    checkServerMessage();
    attachEventListeners();
    if (typeof window.ordiJoue !== 'undefined' && window.ordiJoue) {
        const thinkingLoader = document.getElementById('thinking-loader');
        if (thinkingLoader) thinkingLoader.style.display = 'block';
        setTimeout(() => sendRequest(compMoveUrl), 500);
    }
});

async function loadPuzzleFromLichess() {
    const loader = document.getElementById('board-loader');
    if (loader) loader.style.display = 'flex';
    try {
        const response = await fetch('/puzzle/random-lichess');
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const puzzleData = await response.json();
        if (puzzleData.alreadySolved) {
            showToast(puzzleData.message || "Puzzle du jour déjà résolu.", "info");
            if (loader) loader.style.display = 'none';
            return;
        }
        await loadPuzzleData(puzzleData);
    } catch (error) {
        console.error('Erreur:', error);
        showToast('Erreur: ' + error.message, "error");
        if (loader) loader.style.display = 'none';
    }
}

async function loadPuzzleData(puzzleData) {
    const token = document.querySelector('input[name="_csrf"]')?.value;
    const fd = new FormData();
    fd.append('puzzleId', puzzleData.puzzleId);
    fd.append('fen', puzzleData.fen);
    fd.append('moves', puzzleData.moves);
    fd.append('isLichessPuzzle', 'true');

    try {
        const response = await fetch('/puzzle/load-from-data', {
            method: 'POST',
            body: fd,
            headers: { 'X-CSRF-TOKEN': token }
        });
        if (response.ok) {
            await refreshGameState();
            // SUPPRESSION DU TOAST "Succès" ICI
        } else {
            showToast("Erreur serveur", "error");
        }
    } catch (error) {
        console.error('❌ Erreur:', error);
    } finally {
        const loader = document.getElementById('board-loader');
        if(loader) loader.style.display = 'none';
    }
}