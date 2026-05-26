package com.mirkoebert.export;

import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.handicap.HcpScoreEntity;
import com.mirkoebert.sgi.SingleTestResultEntity;
import com.mirkoebert.sgi.SingleTestResultRepository;
import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HcpCsvExportService {

        private final HcpRepository repo;
        private final SingleTestResultRepository sgirepo;

        String exportAllHcpDataToCsv(final String userId) {
                final List<HcpScoreEntity> all = repo.findByUserId(userId);
                return transform2Csv(all);
        }

        String transform2Csv(final List all){
                val w = new StringWriter();
                try (CSVWriter writer = new CSVWriter(w)){
                        StatefulBeanToCsv<HcpScoreEntity> sbc = new StatefulBeanToCsvBuilder<HcpScoreEntity>(writer).build();
                        sbc.write(all);
                } catch (Exception e) {
                        throw new RuntimeException(e);
                }
                val r = w.toString();
                log.debug("CSV \n {}", r);
                return r;
        }

        String exportAllSgiDataToCsv(final String userId){
                final List<SingleTestResultEntity> all = sgirepo.findAllByUserId(userId);
                return transform2Csv(all);
        }
}
