const changeIcon = document.querySelector('.loadmore');
const x = document.querySelectorAll('.channel-img .none');

const items = document.querySelectorAll('.item');

const navitems = document.querySelectorAll('div.explore ul.nav li.item');

function addNavEvent(item){
  item.addEventListener("click",function () {
    item.className.add("item-selected")
  })
}

function addEvent(item){
  item.addEventListener("click",function () {
    item.
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
  })
}

for(let i = 0; i < navitems.length; i++){
  addNavEvent(navitems[i])
}

items.forEach(addEvent)

let ii = 10;

changeIcon.addEventListener('click', function() {
  fetch('/index/subjects')
      .then(function(response) {
        return response.json();
      })
      .then(function(body) {
        if (!body) {
          return;
        }
        function createItem(item) {
          const div = document.createElement('div');
          div.innerHTML = `
                    <label>
                        <div class="play-btn">
                            <img src="${item.cover}" alt="">
                            <div class="play-mask">
                                <div class="mask"></div>
                                <span class="play-icon"></span>
                                <span class="playing-icon"></span>
                            </div>
                        </div>
                        <div class="txt">
                            <div class="title">
                                <a href="/mhzdetail?subjectId=${item.id}">${item.name}</a>
                            </div>
                            <div class="subtitle">${item.description}</div>
                        </div>
                    </label>
          `;
          return div;
        }
        const channel_img = document.querySelector('.channel-img');
        for (const subjectItem of body) {
          const div = createItem(subjectItem);
          div.className="item"
          channel_img.appendChild(div);
        }
      });
  console.log("fetch")
});