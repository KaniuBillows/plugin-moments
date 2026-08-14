# plugin-moments

[halo-sigs/plugin-moments](https://github.com/halo-sigs/plugin-moments) 的个性化 Fork，在官方能力基础上增加：

- **专属页面** `/moments-page`：按配置标签展示独立瞬间页
- **隐藏标签过滤**：前台公共列表可隐藏指定标签
- **分享链接**：Console / UC 端一键复制瞬间详情链接
- 内置 `moments-page.html` / `moment.html` 模板

![Preview](./images/plugin-moments-preview.png)

## 安装使用

1. 从本仓库 [Releases](https://github.com/KaniuBillows/plugin-moments/releases) 下载 JAR
2. 安装与更新方式见：<https://docs.halo.run/user-guide/plugins>
3. 安装后访问 Console 左侧 **瞬间** 菜单；前台默认路径仍为 `/moments`
4. 专属页面：`/moments-page`（在插件设置中配置专属标签与分页）
5. RSS：`/moments/rss.xml`；搜索同步 type：`moment.moment.halo.run`

## 发布新版本

基于上游版本号打带后缀的 tag，例如上游 `1.16.1` 时：

```bash
git tag v1.16.1-kb.1
git push origin v1.16.1-kb.1
```

GitHub Actions 会自动构建并把 JAR 挂到对应 Release。

同步上游：

```bash
git fetch upstream
git rebase upstream/main
git push --force-with-lease origin main
```

## 官方功能特性

- 支持发布图文、视频、音频等多媒体内容
- 标签筛选、公共 REST API、RSS、Halo 搜索同步

## 主题适配

- **列表** `/moments`（`moments.html`）、**详情** `/moments/{name}`（`moment.html`）
- **专属页** `/moments-page`（`moments-page.html`）
- **Finder API**（`momentFinder`）与公共 REST API

详细文档：

- [主题 API 文档](./dev/theme-api.md)
- [REST API 文档](./dev/rest-api.md)
- [开发环境搭建](./dev/development.md)
