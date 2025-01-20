package com.pcistudio.task.processor.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;

@RequiredArgsConstructor
public class TaskProcessorDataSourceResolver {
    private static final String DEFAULT_DATASOURCE_NAME = "dataSource";
    private static final String DATASOURCE_PROPERTY = "spring.task.processor.datasource";
    private final ApplicationContext applicationContext;
    @SuppressFBWarnings("EI_EXPOSE_REP")
    private DataSource dataSource;

    public DataSource resolveDatasource() {
        if (dataSource == null) {

            String[] beanNamesForType = applicationContext.getBeanNamesForType(DataSource.class);
            if (beanNamesForType.length == 0) {
                throw new IllegalStateException("No DataSource found");
            }
            if (beanNamesForType.length == 1) {
                dataSource = (DataSource) applicationContext.getBean(beanNamesForType[0]);
            } else {
                String datasourceName = applicationContext.getEnvironment().getProperty(DATASOURCE_PROPERTY, DEFAULT_DATASOURCE_NAME);
                dataSource = (DataSource) applicationContext.getBean(datasourceName);
            }
        }

        return dataSource;
    }


}
