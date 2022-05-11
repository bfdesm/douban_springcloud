const btn_shrink = document.querySelector('.btn-shrink');

btn_shrink.addEventListener('click',function (){
    window.scrollTo({
        top: 0,
        behavior: "smooth"
    });
})


const player_play_btn = document.querySelector('.player-play-btn');
const audio = document.querySelector("#audio");
btn = false
player_play_btn.addEventListener('click',function (){
    btn = !btn;
    if(btn){
        audio.play()
        player_play_btn.classList.replace("play", "pause");
    }else{
        audio.pause()
        player_play_btn.classList.replace("pause", "play");
    }
})

window.addEventListener('load', (event) => {

    minute = parseInt(audio.duration/60)
    second = parseInt(audio.duration-minute*60)
    duration.innerHTML = `-`+minute+`:`+second
});

songlist = []
songindex = -1
const title = document.querySelector(".song-info .title")
const artists = document.querySelector(".song-info .artist")

const nextsong = document.querySelector(".options .next")
nextsong.addEventListener("click",function (){
    audio.pause()
    btn = false;
    if(btn){
        audio.play()
        player_play_btn.classList.replace("play", "pause");
    }else{
        audio.pause()
        player_play_btn.classList.replace("pause", "play");
    }
    songindex++
    if(songindex >= songlist.length){
        fetch('/song/random')
            .then(function(response) {
                return response.json();
            })
            .then(function(body) {
                if (!body) {
                    return;
                }

                const title = document.querySelector(".song-info .title")
                const artists = document.querySelector(".song-info .artist")
                artists.innerHTML = ""
                audio.src=body["url"]
                title.innerHTML = body["name"]
                let singerNames = []
                for (const artistItem of body["singerIds"]) {
                    fetch('/getSingerName?singerId='+artistItem)
                        .then(function(response) {
                            return response.json();
                        })
                        .then(function(body1) {
                            if (!body1) {
                                return;
                            }
                            singerNames.push(body1);
                        })
                }

                let song = {
                    "name": body["name"],
                    "url": body["url"],
                    "singerNames":singerNames
                }
                songlist.push(song)
                artists.innerHTML=""
            });
    }else{
        let song = songlist[songindex]
        artists.innerHTML = ""
        audio.src= song["url"]
        title.innerHTML = song["name"]
        let singerNames = song["singerNames"]
        artists.innerHTML=""
    }
    minute = parseInt(audio.duration/60)
    second = parseInt(audio.duration-minute*60)
    duration.innerHTML = `-`+minute+`:`+second
})

const prevsong = document.querySelector(".options .prev")
prevsong.addEventListener("click",function (){
    audio.pause()
    btn = false;
    if(btn){
        audio.play()
        player_play_btn.classList.replace("play", "pause");
    }else{
        audio.pause()
        player_play_btn.classList.replace("pause", "play");
    }

    if(songindex>0){
        songindex--
        let song = songlist[songindex]
        artists.innerHTML = ""
        audio.src= song["url"]
        title.innerHTML = song["name"]
        let singerNames = song["singerNames"]
        artists.innerHTML=""
    }
    minute = parseInt(audio.duration/60)
    second = parseInt(audio.duration-minute*60)
    duration.innerHTML = `-`+minute+`:`+second
})



const duration = document.querySelector('.duration');
const bar_container = document.querySelector(".bar-container")
durationbarbtn = false
duration.addEventListener('click',function (){
    durationbarbtn = !durationbarbtn;
    if(durationbarbtn){
        bar_container.style.opacity = 1;
    }else{
        bar_container.style.opacity = 0;
    }
})

const volume = bar_container.querySelector(".bar-container .bar")

function widthchange(){
    volume.style.left = audio.volume
}

audio.addEventListener("volumechange",function (){
    volume.style.left = audio.volume*50+"px";
})
const progress_cover = document.querySelector(".progress-cover")

progress_cover.style.width=110

audio.addEventListener("timeupdate",function (){
    progress_cover.style.width = 100+audio.currentTime*540.6/audio.duration+"px";
})