package com.suineng.task;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.baidubce.BceClientConfiguration;
import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.services.tsdb.TsdbClient;
import com.baidubce.services.tsdb.model.Filters;
import com.baidubce.services.tsdb.model.GetFieldsResponse;
import com.baidubce.services.tsdb.model.Group;
import com.baidubce.services.tsdb.model.Query;
import com.baidubce.services.tsdb.model.QueryDatapointsResponse;
import com.baidubce.services.tsdb.model.Result;
import com.suineng.bean.Metric;


public class TsdbDao
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TsdbDao.class);

    private final String ACCESSKEY = "b02a7febddb145fcac9aa7be903e7f97"; // access key

    private final String SECRETKEY = "2681428264a347d5bf8cd9d3c780e5f4"; // Secret Key

    private final String ENDPOINT = "snce001.tsdb.iot.gz.baidubce.com"; // 数据库域

    private final String MEASURE = "snce"; // 生产度量

    private final String TAG = "snce002"; // 生产tag

    private TsdbClient client = null;

    public TsdbDao()
    {

    }

    @PostConstruct
    public void init()
    {
        cteate();
    }

    /**
     * init TsdbClient
     */

    private void cteate()
    {
        try
        {
            BceClientConfiguration configuration = new BceClientConfiguration().withEndpoint(
                ENDPOINT).withCredentials(new DefaultBceCredentials(ACCESSKEY, SECRETKEY));
            client = new TsdbClient(configuration);
        }
        catch (Exception e)
        {
            System.out.println(e);
            LOGGER.error("init TsdbClient failed", e);
        }
    }

    // 查询所有的field信息
    public List<String> getAllField()
    {
        LOGGER.info("开始执行定时任务=============================》 当前月数据");
        if (client == null)
        {
            LOGGER.error("getAllField failed, client is null");
            return null;
        }
        List<String> fields = new ArrayList<String>();
        // 获取Field
        GetFieldsResponse response = client.getFields(MEASURE);
        // 打印结果
        for (Map.Entry<String, GetFieldsResponse.FieldInfo> entry : response.getFields().entrySet())
        {
            if (entry.getKey().length() >= 7)
            {
                fields.add(entry.getKey());
            }
        }

        LOGGER.info("getAllField " + fields);
        return fields;
    }

    public List<Metric> getMetric(List<String> fields, long start, long end)
    {
        // 构造查询对象
        List<Query> queries = Arrays.asList(new Query() // 创建Query对象
            .withMetric(MEASURE) // 设置metric
            .withFields(fields) // 设置查询的域名列表，不设置表示查询默认域，和field冲突
            .withTags(Arrays.asList(TAG)) // 设置查询的Tag列表，不设置表示不查询，
            .withFilters(new Filters() // 创建Filters对象
                .withAbsoluteStart(start) // 设置相对的开始时间
                .withAbsoluteEnd(end)) // 设置相对的结束时间
        );

        List<Metric> metrics = new ArrayList<Metric>();
        // 查询数据，返回结果的顺序和请求的field顺序相同
        QueryDatapointsResponse response = client.queryDatapoints(queries);
        for (Result result : response.getResults())
        {
            for (Group group : result.getGroups())
            {
                try
                {
                    if (CollectionUtils.isEmpty(group.getTimeAndValueList()))
                    {
                        continue;
                    }
                    for (Group.TimeAndValue timeAndValue : group.getTimeAndValueList())
                    {
                        for (int index = 0; index < fields.size(); index++ )
                        {
                            Metric metric = new Metric();
                            metric.setId(fields.get(index));
                            metric.setTime(timeAndValue.getTime());

                            // 默认放到第一个value中，后续计算直接从第一个value中获取
                            metric.setValue0((float)timeAndValue.getDoubleValue(index));
                            metrics.add(metric);
                        }
                    }
                }
                catch (IOException e)
                {
                    LOGGER.info("query error", e);
                }
            }
        }

        System.out.println(metrics.size());
        return metrics;
    }

}
