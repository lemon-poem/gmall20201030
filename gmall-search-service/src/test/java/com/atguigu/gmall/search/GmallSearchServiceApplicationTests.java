package com.atguigu.gmall.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsSearchSkuInfo;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchServiceApplicationTests {
	@Reference
	SkuService skuService;
	@Autowired
	JestClient jestClient;
	@Test
	public void contextLoads() throws IOException {
		//jest查询工具
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
		MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName","华为");
		TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId","43");
		boolQueryBuilder.must(matchQueryBuilder);
		boolQueryBuilder.filter(termQueryBuilder);
		searchSourceBuilder.query(boolQueryBuilder);
		searchSourceBuilder.from(0);
		searchSourceBuilder.size(20);
		searchSourceBuilder.highlight(null);
		String s = searchSourceBuilder.toString();
		System.out.println(s);
		/*Search build = new Search.Builder("{\n" +
				"  \"query\": {\n" +
				"    \"bool\": {\n" +
				"      \"filter\":[ {\n" +
				"        \"terms\": {\n" +
				"          \"skuAttrValueList.valueId\": [\n" +
				"            \"43\",\n" +
				"            \"45\"\n" +
				"          ]\n" +
				"        }\n" +
				"      },\n" +
				"      {\n" +
				"        \"term\":{\n" +
				"        \"skuAttrValueList.valueId\":\"43\"\n" +
				"      }}\n" +
				"      ]\n" +
				"      , \"must\": [\n" +
				"        {\n" +
				"          \"match\": {\n" +
				"            \"skuName\": \"华为\"\n" +
				"          }\n" +
				"        }\n" +
				"      ]\n" +
				"    }\n" +
				"  }\n" +
				"}").addIndex("gmall0105").addType("PmsSkuInfo").build();*/
		Search build=new Search.Builder(s).addIndex("gmall0105").addType("PmsSkuInfo").build();
		SearchResult execute = jestClient.execute(build);
		List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);
		List<PmsSearchSkuInfo> pmsSearchSkuInfos=new ArrayList<PmsSearchSkuInfo>();
		for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
			PmsSearchSkuInfo source = hit.source;
			pmsSearchSkuInfos.add(source);
		}

		System.out.println(pmsSearchSkuInfos.size());


	}
	public void put() throws IOException {
		List<PmsSearchSkuInfo> pmsSearchSkuInfos=new ArrayList<PmsSearchSkuInfo>();
		List<PmsSkuInfo> pmsSkuInfos=new ArrayList<>();
		pmsSkuInfos=skuService.getAllSku("123");
		for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
			PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();
			BeanUtils.copyProperties(pmsSkuInfo,pmsSearchSkuInfo);
			pmsSearchSkuInfos.add(pmsSearchSkuInfo);
		}
		for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
			Index put = new Index.Builder(pmsSearchSkuInfo).index("gmall0105").type("PmsSkuInfo").id(pmsSearchSkuInfo.getId()).build();
			jestClient.execute(put);
		}

	}

}
