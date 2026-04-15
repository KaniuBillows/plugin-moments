package run.halo.moments.finders;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;
import run.halo.moments.finders.impl.MomentFinderImpl.MomentQuery;
import run.halo.moments.vo.MomentTagVo;
import run.halo.moments.vo.MomentVo;
import java.util.List;
import java.util.Map;


/**
 * A finder for {@link run.halo.moments.Moment}.
 *
 * @author LIlGG
 * @since 1.0.0
 */
public interface MomentFinder {

    /**
     * List all moments.
     *
     * @return a flux of moment vo.
     */
    Flux<MomentVo> listAll();

    /**
     * List moments by page.
     *
     * @param page page number.
     * @param size page size.
     * @return a mono of list result.
     */
    Mono<ListResult<MomentVo>> list(Integer page, Integer size);

    /**
     * Lists moments by query params.
     *
     * @param params query params see {@link MomentQuery}
     */
    Mono<ListResult<MomentVo>> list(Map<String, Object> params);

    /**
     * List moments by tag.
     *
     * @param tag tag name.
     * @return a flux of moment vo.
     */
    Flux<MomentVo> listBy(String tag);

    Mono<MomentVo> get(String momentName);

    Flux<MomentTagVo> listAllTags();

    /**
     * List all tags, excluding specified hidden tags.
     *
     * @param hiddenTags tags to exclude from the result.
     * @return a flux of moment tag vo.
     */
    Flux<MomentTagVo> listAllTagsExcluding(List<String> hiddenTags);

    Mono<ListResult<MomentVo>> listByTag(int pageNum, Integer pageSize, String tagName);

    /**
     * List moments by tag with pagination, excluding moments that contain any of the hidden tags.
     * When tagName is null/blank, returns all moments except those with hidden tags.
     *
     * @param pageNum   page number.
     * @param pageSize  page size.
     * @param tagName   optional tag filter.
     * @param hiddenTags tags whose moments should be excluded.
     * @return a mono of list result.
     */
    Mono<ListResult<MomentVo>> listByTagExcluding(int pageNum, Integer pageSize,
                                                    String tagName, List<String> hiddenTags);
}
