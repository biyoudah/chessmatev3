    document.addEventListener('DOMContentLoaded', function() {
    const feedContainer = document.getElementById('chess-news-feed');

    const RSS_URL = "https://news.google.com/rss/search?q=echecs&hl=fr&gl=FR&ceid=FR:fr";

    const API_URL = "https://api.rss2json.com/v1/api.json?rss_url=" + encodeURIComponent(RSS_URL);

    fetch(API_URL)
    .then(response => response.json())
    .then(data => {
    feedContainer.innerHTML = '';

    let items = data.items;

    items.sort((a, b) => new Date(b.pubDate) - new Date(a.pubDate));

    items = items.slice(0, 6);

    items.forEach(item => {
    const timeAgo = getTimeAgo(new Date(item.pubDate));

    let title = item.title;
    let source = "Actualité";

    const lastDashIndex = title.lastIndexOf(" - ");
    if (lastDashIndex !== -1) {
    source = title.substring(lastDashIndex + 3);
    title = title.substring(0, lastDashIndex);
}

    const div = document.createElement('div');
    div.className = 'news-item';
    div.innerHTML = `
                    <div class="news-meta">
                        <span class="news-source">${source}</span>
                        <span class="news-time">${timeAgo}</span>
                    </div>
                    <a href="${item.link}" target="_blank" class="news-link">${title}</a>
                `;
    feedContainer.appendChild(div);
});
})
    .catch(error => {
    console.error('Erreur API:', error);
    feedContainer.innerHTML = '<div style="padding:20px; text-align:center; color:#64748b;">Impossible de charger les actus.</div>';
});

    function getTimeAgo(date) {
    const seconds = Math.floor((new Date() - date) / 1000);
    let interval = seconds / 31536000;

    if (interval > 1) return "Il y a " + Math.floor(interval) + " an(s)";
    interval = seconds / 2592000;
    if (interval > 1) return "Il y a " + Math.floor(interval) + " mois";
    interval = seconds / 86400;
    if (interval > 1) return "Il y a " + Math.floor(interval) + " j";
    interval = seconds / 3600;
    if (interval > 1) return "Il y a " + Math.floor(interval) + " h";
    interval = seconds / 60;
    if (interval > 1) return "Il y a " + Math.floor(interval) + " min";
    return "À l'instant";
}
});
