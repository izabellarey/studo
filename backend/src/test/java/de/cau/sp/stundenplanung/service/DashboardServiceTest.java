package de.cau.sp.stundenplanung.service;

import de.cau.sp.stundenplanung.dto.dashboard.AktuelleAenderungDTO;
import de.cau.sp.stundenplanung.dto.dashboard.DashboardDTO;
import de.cau.sp.stundenplanung.repository.AenderungRepository;
import de.cau.sp.stundenplanung.service.AenderungsverlaufService;
import de.cau.sp.stundenplanung.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    AenderungsverlaufService aenderungsverlaufService;

    @Mock
    AenderungRepository aenderungRepository;

    @InjectMocks
    DashboardService service;

    @Test
    void dashboardLiefertBenachrichtigungenUndNeuesteAenderungen() {
        List<AktuelleAenderungDTO> aenderungen = List.of(
                new AktuelleAenderungDTO(),
                new AktuelleAenderungDTO()
        );

        when(aenderungsverlaufService.neuesteAenderungen(5))
                .thenReturn(aenderungen);

        when(aenderungRepository.countDistinctByOffen(true))
                .thenReturn(2L);

        DashboardDTO result = service.dashboard();

        assertEquals(2L, result.getAnzahlOffeneEreignisse());
        assertEquals(aenderungen, result.getAktuelleAenderungen());

        verify(aenderungsverlaufService).neuesteAenderungen(5);
        verify(aenderungRepository).countDistinctByOffen(true);
    }
}