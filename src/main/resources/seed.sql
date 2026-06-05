USE turing;

-- Events
INSERT INTO events (title,description,event_date,event_time,location,status,cover_emoji) VALUES
('2026 秋季招新宣讲会','了解图灵学社的历史、方向和招新流程','2026-09-15','19:00 - 21:00','教学楼 A201','soon','📢'),
('暑期 Hackathon 48 小时编程挑战','自由组队，48 小时内完成一个完整项目','2026-07-20','周六 9:00 起','计算机学院创新实验室','open','💻'),
('AI 与大模型技术分享','邀请业界学长分享大模型训练与部署经验','2026-04-10','19:00 - 21:00','教学楼 B103','past','🤖'),
('春季 Workshop：Git 协作开发','面向新手的 Git 实操课','2026-03-22','14:00 - 17:00','教学楼 C305','past','🔧'),
('ACM 竞赛集训营第一周','讲解基础数据结构和常见算法模板','2025-12-05','周六 14:00 - 17:00','教学楼 A201','past','🏆'),
('从零写一个编译器','学社技术骨干分享编译原理实战','2025-11-18','19:00 - 21:00','教学楼 B103','past','📖'),
('首届校内 Hackathon','48 小时极限编程，200+ 参赛者','2025-10-20','周六 9:00 起','体育馆多功能厅','past','🎉'),
('2025 秋季招新笔试','秋季招新第一轮笔试','2025-09-25','19:00 - 20:30','教学楼 A301-306','past','✏️');

-- FAQs
INSERT INTO faqs (question, answer, author, sort_order) VALUES
('大一新生应该先学哪门编程语言？','推荐 Python 或 C 语言。Python 语法简单，能让新手快速建立编程思维；C 语言紧贴底层，有助于理解内存和指针。','张三',1),
('学社对基础有要求吗？','可以。我们更看重学习热情和投入意愿。加入后会有 mentor 制度帮助新人快速上手。','李四',2),
('想打 ACM 竞赛怎么准备？','三步走：1. 刷 LeetCode Hot 100；2. 上 Codeforces 打周赛从 Div.3 开始；3. 组队参加校赛/省赛积累经验。','王五',3),
('前端和后端哪个方向更好找工作？','两者需求量都很大。建议大一大二两个方向都体验一下再做选择。前端直观但技术迭代快，后端抽象但技术栈相对稳定。','赵六',4),
('学社的活动频率是怎样的？','每周 1-2 次活动，不强制参加。理念是学习为主，社团为辅。','孙七',5),
('怎么加入学社的项目组做实战？','每学期初会有项目组招募，在学社群内发布项目方向和招人要求。也可以自己发起项目在学社内招人组队。','张三',6),
('AI 辅助编程会不会让人变菜？','善用则利，滥用则废。建议先自己写，再让 AI review；而不是让 AI 写，你再改。','李四',7);

-- Resources
INSERT INTO resources (title,description,category,file_type,file_size,link,extract_code,like_count,status) VALUES
('尚硅谷嵌入式技术之Linux应用层开发','尚硅谷嵌入式技术之Linux应用层开发完整教程资料','嵌入式','网盘','-','https://pan.baidu.com/s/1cTvaAvMbnwHzfqfUrtbN-Q?pwd=8h8e','8h8e',0,'published'),
('嵌入式面试技巧','嵌入式方向面试技巧整理与经验分享','嵌入式','网盘','-','https://pan.baidu.com/s/1F9B4_wcA3lRQIJR8TjQrtQ?pwd=9iav','9iav',0,'published'),
('千锋教育C++课件','千锋教育C++完整课件资料','编程语言','网盘','-','https://pan.baidu.com/s/1apRP9D5YjLmCj_dtRD5jfQ?pwd=rtvp','rtvp',0,'published'),
('Docker学习资料','Docker容器技术学习资料合集','DevOps','网盘','-','https://pan.baidu.com/s/1v7eJfmmZNLxaFVLDSzN0TA','sfcc',0,'published'),
('51单片机资料','51单片机入门与进阶学习资料','嵌入式','网盘','-','https://pan.baidu.com/s/1aNuz7TUEUqAqiZhWa3GNLw?pwd=vs7i','vs7i',0,'published'),
('Ubuntu镜像源','Ubuntu系统镜像源文件','操作系统','网盘','-','https://pan.baidu.com/s/1YSOTkFGoPCoYPYTLjc1KSA','jsb8',0,'published'),
('软考相关资料：软件设计师和嵌入式设计师','软考软件设计师与嵌入式系统设计师考试复习资料','考证','网盘','-','https://pan.baidu.com/s/1zvIc48fFhOBb9j0GpEFsiw?pwd=u1n7','u1n7',0,'published'),
('JVM调优','Java虚拟机性能调优学习资料','Java','网盘','-','https://pan.baidu.com/s/1NtKAdMoKc9LsExLGd8a2Gg','n6n8',0,'published'),
('大模型学习资料','AI大模型（LLM）学习资料合集','AI/ML','网盘','-','https://pan.baidu.com/s/1V4RUbmjJukuHosIUiVNlKA','cz4r',0,'published'),
('Oracle数据库','Oracle数据库学习教程与资料','数据库','网盘','-','https://pan.baidu.com/s/1WFROdBofKjdvdsFD-dXs5w','x4qg',0,'published');

-- Members
INSERT INTO members (name,role,department,grade,bio,tags,avatar_gradient,sort_order,is_lead) VALUES
('张明宇','社长','社长团','大三','专注于分布式系统与云原生技术','分布式系统,Go,Kubernetes','linear-gradient(135deg, #FC5531, #e64a2e)',1,TRUE),
('李思涵','副社长','社长团','大三','热爱前端技术与用户体验设计','前端,Vue,TypeScript','linear-gradient(135deg, #409EFF, #337ecc)',2,TRUE),
('王浩然','技术部部长','技术部','大二','擅长后端开发与数据库优化','Java,Spring Boot,MySQL,Docker','linear-gradient(135deg, #67C23A, #529b2e)',3,TRUE),
('赵雨晴','技术部副部长','技术部','大二','对人工智能与机器学习有浓厚兴趣','Python,PyTorch,深度学习,MLOps','linear-gradient(135deg, #E6A23C, #c78b2e)',4,TRUE),
('陈子轩','技术部成员','技术部','大一','算法竞赛爱好者','算法,C++,竞赛','linear-gradient(135deg, #F56C6C, #d45454)',5,FALSE),
('林一鸣','技术部成员','技术部','大一','全栈开发爱好者','React,Node.js,全栈','linear-gradient(135deg, #42b883, #35495e)',6,FALSE),
('刘诗琪','学术部部长','学术部','大三','研究方向为自然语言处理','NLP,Transformer,论文研读','linear-gradient(135deg, #9B59B6, #7d3c98)',7,TRUE),
('孙博文','学术部副部长','学术部','大二','对计算机视觉感兴趣','CV,OpenCV,YOLO,科研','linear-gradient(135deg, #1ABC9C, #148f77)',8,TRUE),
('黄小蕾','学术部成员','学术部','大二','对强化学习感兴趣','强化学习,论文复现,写作','linear-gradient(135deg, #3498DB, #2774a6)',9,FALSE),
('周雅文','运营部部长','运营部','大二','负责学社品牌建设与活动策划','新媒体,UI设计,活动策划,摄影','linear-gradient(135deg, #FF6B81, #d4546b)',10,TRUE),
('吴凯翔','运营部成员','运营部','大一','负责社群管理与线下活动组织','社群运营,视频制作,摄影','linear-gradient(135deg, #3498DB, #2774a6)',11,FALSE),
('郑小雅','运营部成员','运营部','大一','负责公众号推文撰写与排版','文案策划,排版设计,公众号','linear-gradient(135deg, #e056a0, #b8447e)',12,FALSE);

-- Teachers
INSERT INTO teachers (name,title,dept,quote,research,tags,avatar_gradient,sort_order) VALUES
('陈建国','教授 · 博士生导师','计算机学院 · 算法与理论方向','做难而正确的事，写干净而优雅的代码。','算法设计与分析、计算复杂性理论、组合优化','算法设计,计算复杂性,组合优化,图论','linear-gradient(135deg, #667eea 0%, #764ba2 100%)',1),
('刘明远','副教授 · 硕士生导师','软件学院 · 分布式系统方向','系统是计算机科学的骨架。','分布式系统、云计算、微服务架构','分布式系统,云计算,微服务,容器编排','linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',2),
('赵文华','副教授','人工智能学院 · 自然语言处理方向','语言是人类智能的皇冠。','自然语言处理、大语言模型、知识图谱','NLP,大语言模型,知识图谱,文本生成','linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',3),
('孙志刚','讲师 · 博士','计算机学院 · 计算机视觉方向','好奇心是最好的导师。','计算机视觉、图像生成、多模态学习','计算机视觉,图像生成,多模态,深度学习','linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',4);

-- Projects
INSERT INTO projects (name,description,tech_tags,github_link,cover_emoji,member_count,star_count,status) VALUES
('学社官网','基于 VitePress + Vue 3 构建的社团官方网站','Vue 3,VitePress,TypeScript,GSAP','https://github.com/turing/web','🌐',4,128,'active'),
('ACM 训练 OJ 系统','自研在线判题系统，支持 C++/Java/Python','Spring Boot,React,Docker,MySQL','https://github.com/turing/oj','⚡',6,256,'active'),
('课程评价平台','匿名课程评价 + 成绩分布可视化','Python,FastAPI,Vue,PostgreSQL','https://github.com/turing/course-review','📊',3,1024,'completed'),
('智能排课助手','基于遗传算法的自动排课工具','Python,遗传算法,Streamlit','https://github.com/turing/scheduler','📅',2,89,'maintenance'),
('终点小说网','基于微服务架构的在线小说阅读平台，支持海量小说在线阅读、全文检索、智能推荐及用户互动。采用 Spring Cloud 全家桶 + Vue3 前后端分离架构。','Vue3,Spring Cloud,MyBatis-Plus,Redis,RabbitMQ,ElasticSearch','https://github.com/turing/novel-website','📖',3,512,'active');
