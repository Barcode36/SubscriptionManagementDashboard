package it.arsinfo.smd.ui.vaadin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;

import it.arsinfo.smd.SmdService;
import it.arsinfo.smd.data.StatoOperazioneIncasso;
import it.arsinfo.smd.repository.AbbonamentoDao;
import it.arsinfo.smd.repository.AnagraficaDao;
import it.arsinfo.smd.repository.CampagnaDao;
import it.arsinfo.smd.repository.VersamentoDao;

@SpringUI(path = SmdUI.URL_VERSAMENTI)
@Title("Versamenti")
public class VersamentoUI extends SmdUI {

    /**
     * 
     */
    private static final long serialVersionUID = 6407425404499250763L;

    private static final Logger log = LoggerFactory.getLogger(VersamentoUI.class);

    @Autowired
    private VersamentoDao versamentoDao;

    @Autowired
    private AbbonamentoDao abbonamentoDao;

    @Autowired
    private AnagraficaDao anagraficaDao;

    @Autowired
    private CampagnaDao campagnaDao;
    
    @Autowired
    private SmdService smdService;
    
    @Override
    protected void init(VaadinRequest request) {
        super.init(request, "Versamenti");
        
        AbbonamentoVersamentoSearch abbSearch = 
        new AbbonamentoVersamentoSearch(abbonamentoDao,anagraficaDao.findAll(), campagnaDao.findAll());
        VersamentoSearch search = new VersamentoSearch(versamentoDao);
        VersamentoGrid grid = new VersamentoGrid("Versamenti");
        
        OperazioneIncassoAbbonamentoGrid abbonamentiAssociatiGrid = new OperazioneIncassoAbbonamentoGrid("Operazioni Incasso Associate");
        AbbonamentoGrid abbonamentiAssociabiliGrid = new AbbonamentoGrid("Abbonamenti Associabili");

        addSmdComponents(search,grid,abbonamentiAssociatiGrid,abbSearch,abbonamentiAssociabiliGrid);
        
        abbSearch.setVisible(false);
        abbonamentiAssociatiGrid.setVisible(false);
        abbonamentiAssociabiliGrid.setVisible(false);

        grid.getGrid().setHeight("300px");
        abbonamentiAssociabiliGrid.getGrid().setHeight("300px");
        abbonamentiAssociatiGrid.getGrid().setHeight("300px");

        search.setChangeHandler(() -> grid.populate(search.find()));

        grid.setChangeHandler(() -> {
            if (grid.getSelected() != null) {
                abbonamentiAssociatiGrid.populate(smdService.getAssociati(grid.getSelected()));
                abbSearch.setItems(smdService.getAssociabili(grid.getSelected()));
                abbonamentiAssociabiliGrid.populate(abbSearch.find());
                abbSearch.setVisible(true);
            } else {
                abbSearch.setVisible(false);
                abbonamentiAssociatiGrid.setVisible(false);
                abbonamentiAssociabiliGrid.setVisible(false);
            }
        });
        
        abbonamentiAssociabiliGrid.setChangeHandler(() -> {
        });
        
        abbonamentiAssociatiGrid.setChangeHandler(() -> {
        });
        
        abbonamentiAssociatiGrid.addComponentColumn(operazioneIncasso -> {
            Button button = new Button("Storna");
            button.addClickListener(click -> {
                try {
                    smdService.dissocia(operazioneIncasso, getLoggedInUser(),"Eseguita da Versamento UI");
                } catch (Exception e) {
                    Notification.show(e.getMessage(),
                                      Notification.Type.ERROR_MESSAGE);
                    return;
                }
                abbonamentiAssociatiGrid.populate(smdService.getAssociati(grid.getSelected()));
                abbSearch.setItems(smdService.getAssociabili(grid.getSelected()));
                abbonamentiAssociabiliGrid.populate(abbSearch.find());
                abbSearch.setVisible(true);
            });
            
            if (operazioneIncasso.getStatoOperazioneIncasso() != StatoOperazioneIncasso.Incasso) {
                button.setCaption("Non Attivo");
                button.setEnabled(false);
            }
            return button;
        });
        
        abbonamentiAssociabiliGrid.addComponentColumn(abbonamento -> {
            Button button = new Button("Incassa");
            button.addClickListener(click -> {
                try {
                    smdService.incassa(abbonamento, grid.getSelected(),getLoggedInUser(),"Eseguita da Versamento UI");
                } catch (Exception e) {
                    Notification.show(e.getMessage(),
                                      Notification.Type.ERROR_MESSAGE);
                    return;
               }
                
               abbonamentiAssociatiGrid.populate(smdService.getAssociati(grid.getSelected()));
               abbSearch.reset();
               log.info("Incassa: {}", grid.getSelected());
               if (grid.getSelected().getResiduo().signum() > 0) {
                    abbSearch.setItems(smdService.getAssociabili(grid.getSelected()));
                    abbonamentiAssociabiliGrid.populate(abbSearch.find());
                    abbSearch.setVisible(true);
               } else {
                    abbSearch.setVisible(false);
                    abbonamentiAssociabiliGrid.setVisible(false);
               }
            });
            return button;
        });
        
        abbSearch.setChangeHandler(() -> {
            abbonamentiAssociabiliGrid.populate(abbSearch.find());
        });

        grid.populate(search.findAll());
    }
}
