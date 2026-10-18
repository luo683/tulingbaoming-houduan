package com.tuling.config;

import com.tuling.entity.*;
import com.tuling.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 业务种子数据初始化 —— 把原先只存在于 seed.sql 里的社团内容灌进数据库。
 *
 * <p>用 Java 而非 SQL 的原因：schema.sql / seed.sql 是 MySQL 方言（USE / CREATE DATABASE），
 * 在 H2 下会直接语法报错。改成实体写入后，H2 与 MySQL 都能跑，也与 JPA 的 ddl-auto 建表保持一致。
 *
 * <p>幂等：每张表只在“表为空”时灌一次，重复启动不会产生重复数据。
 * 想要重置，删掉 ./data/turing.mv.db 后重启即可。
 */
@Component
@Order(2)   // 必须晚于 DataInitializer（它负责建 admin/demo/user 账号）
public class SeedDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDataInitializer.class);

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private FaqRepository faqRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Override
    public void run(String... args) {
        seedEvents();
        seedFaqs();
        seedResources();
        seedMembers();
        seedTeachers();
        seedProjects();
    }

    // ── 活动 ──
    private void seedEvents() {
        if (eventRepository.count() > 0) { log.info("活动数据已存在，跳过"); return; }
        eventRepository.saveAll(java.util.Arrays.asList(
            event(new Object[]{"2026 秋季招新宣讲会", "了解图灵学社的历史、方向和招新流程", "2026-09-15", "19:00 - 21:00", "教学楼 A201", "soon", "📢"}),
            event(new Object[]{"暑期 Hackathon 48 小时编程挑战", "自由组队，48 小时内完成一个完整项目", "2026-07-20", "周六 9:00 起", "计算机学院创新实验室", "open", "💻"}),
            event(new Object[]{"AI 与大模型技术分享", "邀请业界学长分享大模型训练与部署经验", "2026-04-10", "19:00 - 21:00", "教学楼 B103", "past", "🤖"}),
            event(new Object[]{"春季 Workshop：Git 协作开发", "面向新手的 Git 实操课", "2026-03-22", "14:00 - 17:00", "教学楼 C305", "past", "🔧"}),
            event(new Object[]{"ACM 竞赛集训营第一周", "讲解基础数据结构和常见算法模板", "2025-12-05", "周六 14:00 - 17:00", "教学楼 A201", "past", "🏆"}),
            event(new Object[]{"从零写一个编译器", "学社技术骨干分享编译原理实战", "2025-11-18", "19:00 - 21:00", "教学楼 B103", "past", "📖"}),
            event(new Object[]{"首届校内 Hackathon", "48 小时极限编程，200+ 参赛者", "2025-10-20", "周六 9:00 起", "体育馆多功能厅", "past", "🎉"}),
            event(new Object[]{"2025 秋季招新笔试", "秋季招新第一轮笔试", "2025-09-25", "19:00 - 20:30", "教学楼 A301-306", "past", "✏️"}),
        ));
        log.info("初始化 {} 条活动数据", eventRepository.count());
    }

    // ── FAQ ──
    private void seedFaqs() {
        if (faqRepository.count() > 0) { log.info("FAQ 数据已存在，跳过"); return; }
        faqRepository.saveAll(java.util.Arrays.asList(
            faq(new Object[]{"大一新生应该先学哪门编程语言？", "推荐 Python 或 C 语言。Python 语法简单，能让新手快速建立编程思维；C 语言紧贴底层，有助于理解内存和指针。", "张三", 1}),
            faq(new Object[]{"学社对基础有要求吗？", "可以。我们更看重学习热情和投入意愿。加入后会有 mentor 制度帮助新人快速上手。", "李四", 2}),
            faq(new Object[]{"想打 ACM 竞赛怎么准备？", "三步走：1. 刷 LeetCode Hot 100；2. 上 Codeforces 打周赛从 Div.3 开始；3. 组队参加校赛/省赛积累经验。", "王五", 3}),
            faq(new Object[]{"前端和后端哪个方向更好找工作？", "两者需求量都很大。建议大一大二两个方向都体验一下再做选择。前端直观但技术迭代快，后端抽象但技术栈相对稳定。", "赵六", 4}),
            faq(new Object[]{"学社的活动频率是怎样的？", "每周 1-2 次活动，不强制参加。理念是学习为主，社团为辅。", "孙七", 5}),
            faq(new Object[]{"怎么加入学社的项目组做实战？", "每学期初会有项目组招募，在学社群内发布项目方向和招人要求。也可以自己发起项目在学社内招人组队。", "张三", 6}),
            faq(new Object[]{"AI 辅助编程会不会让人变菜？", "善用则利，滥用则废。建议先自己写，再让 AI review；而不是让 AI 写，你再改。", "李四", 7}),
        ));
        log.info("初始化 {} 条 FAQ 数据", faqRepository.count());
    }

    // ── 资源 ──
    private void seedResources() {
        if (resourceRepository.count() > 0) { log.info("资源数据已存在，跳过"); return; }
        resourceRepository.saveAll(java.util.Arrays.asList(
            resource(new Object[]{"尚硅谷嵌入式技术之Linux应用层开发", "尚硅谷嵌入式技术之Linux应用层开发完整教程资料", "嵌入式", "网盘", "-", "https://pan.baidu.com/s/1cTvaAvMbnwHzfqfUrtbN-Q?pwd=8h8e", "8h8e", 0, "published"}),
            resource(new Object[]{"嵌入式面试技巧", "嵌入式方向面试技巧整理与经验分享", "嵌入式", "网盘", "-", "https://pan.baidu.com/s/1F9B4_wcA3lRQIJR8TjQrtQ?pwd=9iav", "9iav", 0, "published"}),
            resource(new Object[]{"千锋教育C++课件", "千锋教育C++完整课件资料", "编程语言", "网盘", "-", "https://pan.baidu.com/s/1apRP9D5YjLmCj_dtRD5jfQ?pwd=rtvp", "rtvp", 0, "published"}),
            resource(new Object[]{"Docker学习资料", "Docker容器技术学习资料合集", "DevOps", "网盘", "-", "https://pan.baidu.com/s/1v7eJfmmZNLxaFVLDSzN0TA", "sfcc", 0, "published"}),
            resource(new Object[]{"51单片机资料", "51单片机入门与进阶学习资料", "嵌入式", "网盘", "-", "https://pan.baidu.com/s/1aNuz7TUEUqAqiZhWa3GNLw?pwd=vs7i", "vs7i", 0, "published"}),
            resource(new Object[]{"Ubuntu镜像源", "Ubuntu系统镜像源文件", "操作系统", "网盘", "-", "https://pan.baidu.com/s/1YSOTkFGoPCoYPYTLjc1KSA", "jsb8", 0, "published"}),
            resource(new Object[]{"软考相关资料：软件设计师和嵌入式设计师", "软考软件设计师与嵌入式系统设计师考试复习资料", "考证", "网盘", "-", "https://pan.baidu.com/s/1zvIc48fFhOBb9j0GpEFsiw?pwd=u1n7", "u1n7", 0, "published"}),
            resource(new Object[]{"JVM调优", "Java虚拟机性能调优学习资料", "Java", "网盘", "-", "https://pan.baidu.com/s/1NtKAdMoKc9LsExLGd8a2Gg", "n6n8", 0, "published"}),
            resource(new Object[]{"大模型学习资料", "AI大模型（LLM）学习资料合集", "AI/ML", "网盘", "-", "https://pan.baidu.com/s/1V4RUbmjJukuHosIUiVNlKA", "cz4r", 0, "published"}),
            resource(new Object[]{"Oracle数据库", "Oracle数据库学习教程与资料", "数据库", "网盘", "-", "https://pan.baidu.com/s/1WFROdBofKjdvdsFD-dXs5w", "x4qg", 0, "published"}),
        ));
        log.info("初始化 {} 条资源数据", resourceRepository.count());
    }

    // ── 成员 ──
    private void seedMembers() {
        if (memberRepository.count() > 0) { log.info("成员数据已存在，跳过"); return; }
        memberRepository.saveAll(java.util.Arrays.asList(
            member(new Object[]{"李时元", "副社长", "社长团", "23级数据科学与大数据技术", "喜欢写写代码，没事就捣鼓点小东西。人有点宅，但熟了话挺多。骨子里有点倔，想做的事就会默默去搞。平时喝喝咖啡、看看书，就觉得挺踏实。不追求多厉害，靠谱就行。", 1, 1}),
            member(new Object[]{"杜佳", "成员", "学术部", "23级数据科学与大数据技术", "具备Linux和MySQL实战能力，熟悉常用命令及shell脚本，有数据库运维及系统现场部署经验", 0, 10}),
            member(new Object[]{"董消消", "成员", "学术部", "23级数据科学与大数据技术", "喜欢前段开发，可视化大屏，以及spark数据分析", 0, 11}),
            member(new Object[]{"陈磊", "成员", "学术部", "23级数据科学与大数据技术", "喜欢运维相关，学习Linux系统知识，擅长写shell脚本，学习了ansible等工具", 0, 12}),
            member(new Object[]{"韦华西", "成员", "学术部", "25级数据科学与大数据技术", "性格随和，做事踏实认真，有较强的团队意识和责任心", 0, 13}),
            member(new Object[]{"李凯威", "成员", "学术部", "25级数据科学与大数据技术", "熟练使用Claude Code进行AI辅助编程与数据分析，掌握C语言，具备数据清洗与可视化能力", 0, 14}),
            member(new Object[]{"陈流灿", "成员", "学术部", "23级数据科学与大数据技术", "喜欢写代码，热爱社团活动", 0, 15}),
            member(new Object[]{"李映航", "成员", "学术部", "25级数据科学与大数据技术", "好奇心强大，喜欢参加社团活动，乐于参加比赛学习新知识", 0, 16}),
            member(new Object[]{"陈浪", "成员", "学术部", "23级数据科学与大数据技术", "擅长前端网页开发，网络拓扑", 0, 17}),
            member(new Object[]{"冉娜娜", "成员", "学术部", "25级数据科学与大数据技术", "喜欢参加社会活动，主要想学习代码", 0, 18}),
            member(new Object[]{"高招", "成员", "学术部", "25级数据科学与大数据技术", "平时就爱敲代码，没事写写程序，遇到bug慢慢排查", 0, 19}),
            member(new Object[]{"王佳蓉", "成员", "学术部", "25级数据科学与大数据技术", "热爱计算机专业，日常主动学习编程基础知识", 0, 20}),
            member(new Object[]{"杨志强", "成员", "学术部", "25级数据科学与大数据技术", "暂无简介", 0, 21}),
            member(new Object[]{"熊永", "成员", "学术部", "23级数据科学与大数据技术", "对写代码较为感兴趣", 0, 22}),
            member(new Object[]{"罗望博", "部长", "实践部", "25级数据科学与大数据技术", "热爱社团活动", 1, 30}),
            member(new Object[]{"唐丽佳", "成员", "实践部", "25级数据科学与大数据技术", "熟悉数据整理、基础建模等技能，逻辑思维清晰", 0, 31}),
            member(new Object[]{"李玉肖", "成员", "实践部", "25级数据科学与大数据技术", "热爱社团活动，喜欢写代码", 0, 32}),
            member(new Object[]{"曾静", "成员", "实践部", "25级数据科学与大数据技术", "喜欢写代码，本人还在学习中", 0, 33}),
            member(new Object[]{"袁国静", "成员", "实践部", "25级数据科学与大数据技术", "热爱实践类活动，踏实完成分配任务", 0, 34}),
            member(new Object[]{"兰胜康", "成员", "实践部", "25级数据科学与大数据技术", "喜欢研究代码逻辑，对游戏开发感兴趣", 0, 35}),
            member(new Object[]{"任华亮", "成员", "实践部", "25级数据科学与大数据技术", "喜欢研究AI、掌握C语言、有Python知识", 0, 36}),
            member(new Object[]{"王忠良", "成员", "实践部", "24级数据科学与大数据技术", "热爱代码编程，向往科创与学科竞赛", 0, 37}),
            member(new Object[]{"黄彬", "成员", "实践部", "25级数据科学与大数据技术", "学习过C语言编程，完成音乐播放器开发项目", 0, 38}),
            member(new Object[]{"李宗伟", "成员", "实践部", "25级数据科学与大数据技术", "喜欢钻研专业知识，热爱社团", 0, 39}),
            member(new Object[]{"李豪浪", "成员", "实践部", "25级数据科学与大数据技术", "代码初学者", 0, 40}),
            member(new Object[]{"林晨", "成员", "实践部", "24级数据科学与大数据技术", "善于沟通，会写简单的代码", 0, 41}),
            member(new Object[]{"郭凯", "成员", "实践部", "23级数据科学与大数据技术", "喜欢代码", 0, 42}),
            member(new Object[]{"罗闽奇", "部长", "宣传部", "24级数据科学与大数据技术", "对代码感兴趣，喜欢拍照记录", 1, 50}),
            member(new Object[]{"帅宗会", "成员", "宣传部", "25级数据科学与大数据技术", "善于团队协作，积极配合任务", 0, 51}),
            member(new Object[]{"张旭峰", "成员", "宣传部", "25级数据科学与大数据技术", "喜欢程序设计方面的内容", 0, 52}),
            member(new Object[]{"陈浩宇", "成员", "宣传部", "25级数据科学与大数据技术", "热爱编程，对算法和AI基础充满好奇", 0, 53}),
            member(new Object[]{"徐欢", "成员", "宣传部", "25级数据科学与大数据技术", "喜爱代码", 0, 54}),
            member(new Object[]{"苏辛谊", "成员", "宣传部", "24级数据科学与大数据技术", "热爱运动，爱与人交流", 0, 55}),
            member(new Object[]{"张琦", "成员", "宣传部", "25级数据科学与大数据技术", "熟练基础编程知识，擅长活动统筹", 0, 56}),
            member(new Object[]{"王慧", "部长", "办公室", "25级数据科学与大数据技术", "热爱社团活动", 1, 60}),
            member(new Object[]{"宋丽", "成员", "办公室", "25级数据科学与大数据技术", "喜欢制作手工，听音乐，责任心强", 0, 61}),
            member(new Object[]{"杨巧巧", "成员", "办公室", "25级数据科学与大数据技术", "热爱社团活动，喜欢计算机相关知识", 0, 62}),
            member(new Object[]{"龙江蓉", "部长", "外联部", "25级数据科学与大数据技术", "热爱社团活动，喜欢AI", 1, 70}),
            member(new Object[]{"颜水洪", "成员", "外联部", "25级数据科学与大数据技术", "热爱社团活动", 0, 71}),
            member(new Object[]{"罗榆宏", "成员", "外联部", "25级数据科学与大数据技术", "踏实稳重、责任心强，具备良好沟通协调能力", 0, 72}),
        ));
        log.info("初始化 {} 条成员数据", memberRepository.count());
    }

    // ── 教师 ──
    private void seedTeachers() {
        if (teacherRepository.count() > 0) { log.info("教师数据已存在，跳过"); return; }
        teacherRepository.saveAll(java.util.Arrays.asList(
            teacher(new Object[]{"陈熙", "教授", "大数据系", "2011年西南交通大学博士，从事图像处理、模式识别与生物特征识别方面研究", 101}),
            teacher(new Object[]{"曹永锋", "教授", "大数据系", "通信与信息系统工学博士（武汉大学），教授，博导", 102}),
            teacher(new Object[]{"欧卫华", "教授", "大数据系", "华中科技大学博士，大数据与计算机科学学院院长", 103}),
            teacher(new Object[]{"谢刚", "教授", "大数据系", "贵州大学博士，中国计算机学会高级会员", 104}),
            teacher(new Object[]{"杨华", "教授", "大数据系", "毕业于武汉大学，教授，发表论文20余篇", 105}),
            teacher(new Object[]{"周永辉", "教授", "大数据系", "浙江大学博士，北京大学博士后，博士生导师", 106}),
            teacher(new Object[]{"陈延利", "副教授", "大数据系", "工学博士，网络空间安全学科负责人", 201}),
            teacher(new Object[]{"付苏嘉", "副教授", "大数据系", "暂无简介", 202}),
            teacher(new Object[]{"傅明丽", "副教授", "大数据系", "暂无简介", 203}),
            teacher(new Object[]{"李伟岸", "副教授", "大数据系", "暂无简介", 204}),
            teacher(new Object[]{"刘小雪", "副教授", "大数据系", "暂无简介", 205}),
            teacher(new Object[]{"任福佳", "副教授", "大数据系", "暂无简介", 206}),
            teacher(new Object[]{"舒丽娟", "副教授", "大数据系", "2017年毕业于上海社会科学院", 207}),
            teacher(new Object[]{"苏彩霞", "副教授", "大数据系", "暂无简介", 208}),
            teacher(new Object[]{"田浩", "副教授", "大数据系", "华东师范大学博士，香港大学博士后", 209}),
            teacher(new Object[]{"吴卉男", "副教授", "大数据系", "暂无简介", 210}),
            teacher(new Object[]{"徐盛", "副教授", "大数据系", "暂无简介", 211}),
            teacher(new Object[]{"颜焕", "副教授", "大数据系", "合肥工业大学博士，硕士生导师", 212}),
            teacher(new Object[]{"杨阿祧", "副教授", "大数据系", "博士，研究方向是协同计算、教育人工智能", 213}),
            teacher(new Object[]{"杨柳", "副教授", "大数据系", "暂无简介", 214}),
            teacher(new Object[]{"张琦", "副教授", "大数据系", "暂无简介", 215}),
            teacher(new Object[]{"耿楠", "讲师", "大数据系", "暂无简介", 301}),
            teacher(new Object[]{"胡家磊", "讲师", "大数据系", "暂无简介", 302}),
            teacher(new Object[]{"姜希", "讲师", "大数据系", "暂无简介", 303}),
            teacher(new Object[]{"雷霈", "讲师", "大数据系", "暂无简介", 304}),
            teacher(new Object[]{"刘健", "讲师", "大数据系", "暂无简介", 305}),
            teacher(new Object[]{"王鹏举", "讲师", "大数据系", "暂无简介", 306}),
            teacher(new Object[]{"余滨", "讲师", "大数据系", "厦门大学博士，研究移动智能Agent、机器学习", 307}),
            teacher(new Object[]{"胡圣波", "教授", "信息工程系", "工学博士，大数据与计算机科学学院教授、院长", 401}),
            teacher(new Object[]{"陈婀娜", "副教授", "信息工程系", "暂无简介", 501}),
            teacher(new Object[]{"勾文铀", "副教授", "信息工程系", "国防科技大学博士", 502}),
            teacher(new Object[]{"胡波", "副教授", "信息工程系", "暂无简介", 503}),
            teacher(new Object[]{"付婷", "副教授", "信息工程系", "暂无简介", 504}),
            teacher(new Object[]{"雷涛", "副教授", "信息工程系", "贵州大学博士，东南大学博士后", 505}),
            teacher(new Object[]{"莫金容", "副教授", "信息工程系", "研究传感器网络和空天组网", 506}),
            teacher(new Object[]{"王建威", "副教授", "信息工程系", "韩国成均馆大学博士，信息工程系主任", 507}),
            teacher(new Object[]{"鄢婷婷", "副教授", "信息工程系", "贵州师范大学理学博士", 508}),
            teacher(new Object[]{"柴永强", "讲师", "信息工程系", "暂无简介", 601}),
            teacher(new Object[]{"方兴", "讲师", "信息工程系", "暂无简介", 602}),
            teacher(new Object[]{"郭子义", "讲师", "信息工程系", "暂无简介", 603}),
            teacher(new Object[]{"罗云", "讲师", "信息工程系", "暂无简介", 604}),
            teacher(new Object[]{"明芳", "讲师", "信息工程系", "暂无简介", 605}),
            teacher(new Object[]{"彭嫚", "讲师", "信息工程系", "贵州大学博士", 606}),
            teacher(new Object[]{"秦婵婵", "讲师", "信息工程系", "华中师范大学博士", 607}),
            teacher(new Object[]{"施燕峰", "讲师", "信息工程系", "暂无简介", 608}),
            teacher(new Object[]{"舒恒", "讲师", "信息工程系", "暂无简介", 609}),
            teacher(new Object[]{"王永容", "讲师", "信息工程系", "暂无简介", 610}),
            teacher(new Object[]{"向天宇", "讲师", "信息工程系", "华中师范大学博士", 611}),
            teacher(new Object[]{"张婧", "讲师", "信息工程系", "暂无简介", 612}),
            teacher(new Object[]{"刘彬", "教授", "计算机系", "毕业于中南大学，贵州计算机学会常务理事", 701}),
            teacher(new Object[]{"林欣", "教授", "计算机系", "厦门大学硕士，从事大数据智能技术研究", 702}),
            teacher(new Object[]{"王红兵", "教授", "计算机系", "长期从事人工智能应用研究", 703}),
            teacher(new Object[]{"陈家民", "副教授", "计算机系", "中南大学博士，研究图自动机器学习", 801}),
            teacher(new Object[]{"陈仕先", "副教授", "计算机系", "暂无简介", 802}),
            teacher(new Object[]{"代亮", "副教授", "计算机系", "暂无简介", 803}),
            teacher(new Object[]{"董玟颖", "副教授", "计算机系", "暂无简介", 804}),
            teacher(new Object[]{"冷继兵", "副教授", "计算机系", "暂无简介", 805}),
            teacher(new Object[]{"罗佳", "副教授", "计算机系", "暂无简介", 806}),
            teacher(new Object[]{"冉桂萍", "副教授", "计算机系", "暂无简介", 807}),
            teacher(new Object[]{"冉义军", "副教授", "计算机系", "理学博士，研究复杂网络与计算传播学", 808}),
            teacher(new Object[]{"王安志", "副教授", "计算机系", "四川大学博士，研究AI与计算机视觉", 809}),
            teacher(new Object[]{"王祥斌", "副教授", "计算机系", "暂无简介", 810}),
            teacher(new Object[]{"熊祥光", "副教授", "计算机系", "研究可逆数据隐藏与区块链技术", 811}),
            teacher(new Object[]{"肖昌成", "副教授", "计算机系", "研究计算机视觉与多模态融合", 812}),
            teacher(new Object[]{"谢杰航", "副教授", "计算机系", "南开大学博士，研究多模态融合", 813}),
            teacher(new Object[]{"徐凯", "副教授", "计算机系", "暂无简介", 814}),
            teacher(new Object[]{"熊志刚", "副教授", "计算机系", "暂无简介", 815}),
            teacher(new Object[]{"杨焱", "副教授", "计算机系", "暂无简介", 816}),
            teacher(new Object[]{"尹丹", "副教授", "计算机系", "暂无简介", 817}),
            teacher(new Object[]{"尹兰", "副教授", "计算机系", "暂无简介", 818}),
            teacher(new Object[]{"艾大清", "讲师", "计算机系", "暂无简介", 901}),
            teacher(new Object[]{"唐俊林", "讲师", "计算机系", "暂无简介", 902}),
            teacher(new Object[]{"贡庆", "实验室人员", "实验室", "暂无简介", 1001}),
            teacher(new Object[]{"李刊", "实验室人员", "实验室", "暂无简介", 1002}),
            teacher(new Object[]{"刘新", "实验室人员", "实验室", "暂无简介", 1003}),
            teacher(new Object[]{"宋小伟", "实验室人员", "实验室", "暂无简介", 1004}),
            teacher(new Object[]{"陶昆", "实验室人员", "实验室", "暂无简介", 1005}),
            teacher(new Object[]{"王喜元", "实验室人员", "实验室", "暂无简介", 1006}),
            teacher(new Object[]{"杨飚", "实验室人员", "实验室", "暂无简介", 1007}),
            teacher(new Object[]{"王文利", "实验室人员", "实验室", "暂无简介", 1008}),
            teacher(new Object[]{"龙兆", "实验室人员", "实验室", "暂无简介", 1009}),
            teacher(new Object[]{"肖子韬", "实验室人员", "实验室", "暂无简介", 1010}),
        ));
        log.info("初始化 {} 条教师数据", teacherRepository.count());
    }

    // ── 项目 ──
    private void seedProjects() {
        if (projectRepository.count() > 0) { log.info("项目数据已存在，跳过"); return; }
        projectRepository.saveAll(java.util.Arrays.asList(
            project(new Object[]{"学社官网", "基于 VitePress + Vue 3 构建的社团官方网站", "Vue 3,VitePress,TypeScript,GSAP", "https://github.com/turing/web", "🌐", 4, 128, "active"}),
            project(new Object[]{"ACM 训练 OJ 系统", "自研在线判题系统，支持 C++/Java/Python", "Spring Boot,React,Docker,MySQL", "https://github.com/turing/oj", "⚡", 6, 256, "active"}),
            project(new Object[]{"课程评价平台", "匿名课程评价 + 成绩分布可视化", "Python,FastAPI,Vue,PostgreSQL", "https://github.com/turing/course-review", "📊", 3, 1024, "completed"}),
            project(new Object[]{"智能排课助手", "基于遗传算法的自动排课工具", "Python,遗传算法,Streamlit", "https://github.com/turing/scheduler", "📅", 2, 89, "maintenance"}),
            project(new Object[]{"终点小说网", "基于微服务架构的在线小说阅读平台，支持海量小说在线阅读、全文检索、智能推荐及用户互动。采用 Spring Cloud 全家桶 + Vue3 前后端分离架构。", "Vue3,Spring Cloud,MyBatis-Plus,Redis,RabbitMQ,ElasticSearch", "https://github.com/turing/novel-website", "📖", 3, 512, "active"}),
        ));
        log.info("初始化 {} 条项目数据", projectRepository.count());
    }

    // ══════════ 构造辅助（把 seed.sql 的元组转成实体） ══════════

    private static Event event(Object[] v) {
        Event e = new Event();
        e.setTitle((String) v[0]);
        e.setDescription((String) v[1]);
        e.setEventDate(LocalDate.parse((String) v[2]));
        e.setEventTime((String) v[3]);
        e.setLocation((String) v[4]);
        e.setStatus((String) v[5]);
        e.setCoverEmoji((String) v[6]);
        return e;
    }

    private static Faq faq(Object[] v) {
        Faq f = new Faq();
        f.setQuestion((String) v[0]);
        f.setAnswer((String) v[1]);
        f.setAuthor((String) v[2]);
        f.setSortOrder((Integer) v[3]);
        return f;
    }

    private static Resource resource(Object[] v) {
        Resource r = new Resource();
        r.setTitle((String) v[0]);
        r.setDescription((String) v[1]);
        r.setCategory((String) v[2]);
        r.setFileType((String) v[3]);
        r.setFileSize((String) v[4]);
        r.setLink((String) v[5]);
        r.setExtractCode((String) v[6]);
        r.setLikeCount((Integer) v[7]);
        r.setStatus((String) v[8]);
        return r;
    }

    private static Member member(Object[] v) {
        Member m = new Member();
        m.setName((String) v[0]);
        m.setRole((String) v[1]);
        m.setDepartment((String) v[2]);
        m.setGrade((String) v[3]);
        m.setBio((String) v[4]);
        m.setIsLead(((Integer) v[5]) != 0);
        m.setSortOrder((Integer) v[6]);
        return m;
    }

    private static Teacher teacher(Object[] v) {
        Teacher t = new Teacher();
        t.setName((String) v[0]);
        t.setTitle((String) v[1]);
        t.setDept((String) v[2]);
        t.setQuote((String) v[3]);
        t.setSortOrder((Integer) v[4]);
        return t;
    }

    private static Project project(Object[] v) {
        Project p = new Project();
        p.setName((String) v[0]);
        p.setDescription((String) v[1]);
        p.setTechTags((String) v[2]);
        p.setGithubLink((String) v[3]);
        p.setCoverEmoji((String) v[4]);
        p.setMemberCount((Integer) v[5]);
        p.setStarCount((Integer) v[6]);
        p.setStatus((String) v[7]);
        return p;
    }
}
