package com.githuib.zhengchalei.yanglegeyang

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import java.util.concurrent.ConcurrentHashMap


@RestController
@SpringBootApplication
class YanglegeyangApplication {

    val log: Logger = LoggerFactory.getLogger(YanglegeyangApplication::class.java)

    var client: RestTemplate = RestTemplateBuilder().build()

    val map = ConcurrentHashMap<String, Int>()

    fun check(uid: String): Int {
        val count = map[uid]
        if (count == null) {
            map[uid] = 1
        } else {
            map[uid] = count.plus(1)
        }
        return count ?: return 1
    }

    @GetMapping("/{uid}/{name}")
    fun go(@PathVariable(required = true) uid: Int, @PathVariable name: String): String {
        val count = check(uid.toString())
        if (count > 20) return "能不能别一直的怼着我刷, 你去看看GitHub吧"
        log.info("map size: {}, uid: {}, count: {}", map.size, uid, count)
        val userInfo = userInfo(uid.toString())
        val token = token(userInfo, name)
        Thread() {
            topicGameOver(token)
            for (i in 0 until 1) {
                gameOver(token)
            }
        }.start()
        return "已经开始刷咯!"
    }

//    @GetMapping("/{uid}/{count}/{name}")
//    fun go(@PathVariable(required = true) uid: String, @PathVariable count: Int, @PathVariable name: String): String {
//        val userInfo = userInfo(uid)
//        val token = token(userInfo, name)
//        Thread() {
//            topicGameOver(token)
//            for (i in 0 until count) {
//                gameOver(token)
//            }
//        }.start()
//        return "已经开始刷咯, 隐藏的小技巧被你发现了!"
//    }

    fun userInfo(uid: String): JSONObject {
        val url =
            "https://cat-match.easygame2021.com/sheep/v1/game/user_info?uid=${uid}&t=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2OTQ0MDU0MjMsIm5iZiI6MTY2MzMwMzIyMywiaWF0IjoxNjYzMzAxNDIzLCJqdGkiOiJDTTpjYXRfbWF0Y2g6bHQxMjM0NTYiLCJvcGVuX2lkIjoiIiwidWlkIjoxMDg0MzMxMjgsImRlYnVnIjoiIiwibGFuZyI6IiJ9.oT1OY9XokZmHt1Hzifc8ILF1U-xQxY-itXNaeLj02R8"
        val body = client.getForObject<String>(url)
        val jsonObject = JSON.parseObject(body).getJSONObject("data")
        log.info("刷的人: {}", jsonObject)
        return jsonObject
    }

    fun token(userInfo: JSONObject, name: String): String {
        val avatar = "https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTKbPy04717qMxC8NZZx2bEy0bntr59KFibibnAkzx6RGGtGU6ibxZcMwN0PfmWsHj8WXia1WoE9MMiabSw/132"
        val url = "https://cat-match.easygame2021.com/sheep/v1/user/login_oppo?uid=${userInfo.getString("wx_open_id")}&nick_name=${name}&avatar=${avatar}&sex=1"
        val body = client.postForObject(url, "", String::class.java)
        return JSON.parseObject(body).getJSONObject("data").getString("token")
    }

    fun topicGameOver(t: String) {
        val url =
            "https://cat-match.easygame2021.com/sheep/v1/game/topic_game_over?rank_score=1&rank_state=1&rank_time=1&rank_role=1&skin=1"
        val header = HttpHeaders()
        header.add("t", t)
        val entity = HttpEntity<String>(header)
        client.exchange(url, HttpMethod.GET, entity, String::class.java)
    }

    fun gameOver(t: String) {
        val url =
            "https://cat-match.easygame2021.com/sheep/v1/game/game_over?rank_score=1&rank_state=1&rank_time=365&rank_role=1&skin=1"
        val header = HttpHeaders()
        header.add("t", t)
        val entity = HttpEntity<String>(header)
        client.exchange(url, HttpMethod.GET, entity, String::class.java)
    }

}

fun main(args: Array<String>) {
    runApplication<YanglegeyangApplication>(*args)
}
