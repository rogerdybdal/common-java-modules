package no.nav.fo.feed.producer;

import no.nav.fo.feed.common.FeedElement;

import java.util.stream.Stream;

@FunctionalInterface
public interface FeedProvider<DOMAINOBJECT extends Comparable<DOMAINOBJECT>> {
    Stream<FeedElement<DOMAINOBJECT>> fetchData(String id, int pageSize);
}