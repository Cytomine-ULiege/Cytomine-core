package be.cytomine.utils;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.http.HttpStatus;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class WireMockHelper {

    private final WireMockServer wireMockServer;

    public WireMockHelper(WireMockServer wireMockServer) {
        this.wireMockServer = wireMockServer;
    }

    public void stubElasticSearchApi(long id, int filterSize) {
        wireMockServer.stubFor(post(urlPathMatching("/properties/_search"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withHeader("X-Elastic-Product", "Elasticsearch")
                .withBody("{\"took\":911," +
                    "\"timed_out\":false," +
                    "\"_shards\":{\"total\":1,\"successful\":1,\"skipped\":0,\"failed\":0}," +
                    "\"hits\":{\"total\":{\"value\":1,\"relation\":\"eq\"}," +
                    "\"max_score\":1,\"hits\":[{\"_index\":\"properties\",\"_id\":\"1041\",\"_score\":1," +
                    "\"_version\":1," +
                    "\"_source\":{\"value\":\"test-value\",\"@timestamp\":\"2024-08-21T09:04:00.694446626Z\"," +
                    "\"domain_class_name\":\"be.cytomine.domain.image.AbstractImage\",\"domain_ident\":201," +
                    "\"key\":\"test-key\",\"id\":1041,\"@version\":\"1\"}}]}," +
                    "\"aggregations\":{\"lterms#domain_ident_count\":{\"buckets\":" +
                    "[{\"key\":" + id + ",\"doc_count\":" + filterSize + "}]}}}"
                )
                .withStatus(HttpStatus.OK.value())
            )
        );
    }
}
