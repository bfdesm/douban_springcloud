const changeIcon = document.querySelector('.change');

changeIcon.addEventListener('click', function() {
    fetch('/singer/random')
        .then(function(response) {
            return response.json();
        })
        .then(function(body) {
            if (!body) {
                return;
            }

            function createItem(item) {
                const li = document.createElement('li');
                li.innerHTML = `
                        <div class="icon" style="background:url(${item.avatar}) no-repeat center;background-size: contain;">
                            <!-- 选择后，头像上有对勾 -->
                            <div class="shadow">
                                <span></span>
                            </div>
                        </div>
                        <h4>${item.name}</h4>
                        <div class="like">喜欢</div>
                    `;
                return li;
            }

            const artistsList = document.querySelector('.artists-list');
            artistsList.innerHTML = '';
            for (const singerItem of body) {
                const li = createItem(singerItem);
                artistsList.appendChild(li);
                addEvent(li);
            }

        });
});

const artistsListLi = document.querySelectorAll('.artists-list li');
artistsListLi.forEach(addEvent)

const changeList = document.querySelector('.changed-list');

function addEvent(artistsList){
    artistsList.addEventListener('click',function (){
        function createItem(item){
            let li = document.createElement('li');
            let sty = m.style.background;
            sty = sty.replaceAll('"',"");
            li.innerHTML = `
                  <!-- 遮罩 -->
                  <div class="icon" style="background: ${sty}">
                      <div class="shadow">
                        <span class="close"></span>
                      </div>
                  </div>`;
            li.addEventListener('click',function (){
                li.remove();
            });
            return li;
        }
        let m = artistsList.querySelector(".icon");
        changeList.appendChild(createItem(m));
    })
}
