package run.halo.moments;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static run.halo.app.theme.router.PageUrlUtils.totalPage;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.ReactiveSettingFetcher;
import run.halo.app.theme.router.PageUrlUtils;
import run.halo.app.theme.router.UrlContextListResult;
import run.halo.moments.finders.MomentFinder;
import run.halo.moments.vo.MomentVo;


/**
 * Provides a <code>/moments</code> route for the topic end to handle routing.
 * Topic should contain a <code>moments.html</code> file.
 * <p>
 * In order to handle pagination, an additional /moments/page/{page} route has been adapted.
 * </p>
 *
 * @author LIlGG
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class MomentRouter {
    private static final String TAG_PARAM = "tag";
    private final MomentFinder momentFinder;

    private final ReactiveSettingFetcher settingFetcher;

    @Bean
    RouterFunction<ServerResponse> momentRouterFunction() {
        return route(GET("/moments").or(GET("/moments/page/{page:\\d+}")), handlerFunction())
            .andRoute(GET("/moments/{momentName:\\S+}"), handlerMomentDefault())
            .andRoute(GET("/moments-page"), handlerExclusivePage());
    }

    private HandlerFunction<ServerResponse> handlerMomentDefault() {
        return request -> {
            String momentName = request.pathVariable("momentName");
            return ServerResponse.ok().render("moment",
                Map.of("moment", momentFinder.get(momentName),
                    ModelConst.TEMPLATE_ID, "moment",
                    "title", getMomentTitle())
            );
        };
    }

    private HandlerFunction<ServerResponse> handlerFunction() {
        return request -> ServerResponse.ok().render("moments",
            Map.of("moments", momentList(request),
                ModelConst.TEMPLATE_ID, "moments",
                "tags", getHiddenTags().flatMapMany(momentFinder::listAllTagsExcluding),
                "title", getMomentTitle()
            )
        );
    }

    /**
     * Handler for the exclusive moments page (/moments-page).
     * Reads the exclusive page settings and renders the moments-page template.
     */
    private HandlerFunction<ServerResponse> handlerExclusivePage() {
        return request -> {
            return settingFetcher.get("exclusivePage")
                .flatMap(setting -> {
                    String tag = setting.get("exclusivePageTag").asText("");
                    String pageTitle = setting.get("exclusivePageTitle").asText("瞬间");
                    int pageSize = setting.get("exclusivePageSize").asInt(20);

                    if (StringUtils.isBlank(tag)) {
                        // No tag configured, show config missing page
                        Map<String, Object> model = new HashMap<>();
                        model.put("configMissing", true);
                        model.put("pageTitle", pageTitle);
                        model.put(ModelConst.TEMPLATE_ID, "moments-page");
                        return ServerResponse.ok().render("moments-page", model);
                    }

                    // Read sort and page from query params
                    String sortDir = request.queryParam("sort").orElse("desc");
                    int page = NumberUtils.toInt(
                        request.queryParam("page").orElse("1"), 1);

                    Map<String, Object> model = new HashMap<>();
                    model.put("configMissing", false);
                    model.put("tag", tag);
                    model.put("pageTitle", pageTitle);
                    model.put("sortDir", sortDir);
                    model.put("page", page);
                    model.put("size", pageSize);
                    model.put(ModelConst.TEMPLATE_ID, "moments-page");
                    return ServerResponse.ok().render("moments-page", model);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // No exclusive page settings at all
                    Map<String, Object> model = new HashMap<>();
                    model.put("configMissing", true);
                    model.put("pageTitle", "瞬间");
                    model.put(ModelConst.TEMPLATE_ID, "moments-page");
                    return ServerResponse.ok().render("moments-page", model);
                }));
        };
    }

    Mono<String> getMomentTitle() {
        return this.settingFetcher.get("base")
            .map(setting -> setting.get("title").asText("瞬间"))
            .defaultIfEmpty("瞬间");
    }

    /**
     * Get hidden tags from base settings.
     */
    Mono<List<String>> getHiddenTags() {
        return this.settingFetcher.get("base")
            .map(setting -> {
                String hiddenTagsStr = setting.get("hiddenTags").asText("");
                if (StringUtils.isBlank(hiddenTagsStr)) {
                    return Collections.<String>emptyList();
                }
                return Arrays.stream(hiddenTagsStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            })
            .defaultIfEmpty(Collections.emptyList());
    }

    private Mono<UrlContextListResult<MomentVo>> momentList(ServerRequest request) {
        String path = request.path();
        String tagVal = request.queryParam(TAG_PARAM)
            .filter(StringUtils::isNotBlank)
            .orElse(null);
        int pageNum = pageNumInPathVariable(request);
        String tag = tagPathQueryParam(request);
        return Mono.zip(
            this.settingFetcher.get("base")
                .map(item -> item.get("pageSize").asInt(10))
                .defaultIfEmpty(10),
            getHiddenTags()
        ).flatMap(tuple -> {
            int pageSize = tuple.getT1();
            List<String> hiddenTags = tuple.getT2();
            return momentFinder.listByTagExcluding(pageNum, pageSize, tag, hiddenTags)
                .map(list -> new UrlContextListResult.Builder<MomentVo>()
                    .listResult(list)
                    .nextUrl(appendTagParamIfPresent(
                        PageUrlUtils.nextPageUrl(path, totalPage(list)), tagVal)
                    )
                    .prevUrl(appendTagParamIfPresent(PageUrlUtils.prevPageUrl(path), tagVal))
                    .build()
                );
        });
    }

    String appendTagParamIfPresent(String uriString, String tagValue) {
        return UriComponentsBuilder.fromUriString(uriString)
            .queryParamIfPresent(TAG_PARAM, Optional.ofNullable(tagValue))
            .build()
            .toString();
    }

    private int pageNumInPathVariable(ServerRequest request) {
        String page = request.pathVariables().get("page");
        return NumberUtils.toInt(page, 1);
    }

    private String tagPathQueryParam(ServerRequest request) {
        return request.queryParam(TAG_PARAM).orElse(null);
    }
}
