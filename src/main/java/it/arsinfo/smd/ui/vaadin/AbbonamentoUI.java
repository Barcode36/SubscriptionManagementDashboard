package it.arsinfo.smd.ui.vaadin;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

import it.arsinfo.smd.SmdService;
import it.arsinfo.smd.data.Anno;
import it.arsinfo.smd.data.StatoAbbonamento;
import it.arsinfo.smd.entity.Abbonamento;
import it.arsinfo.smd.entity.Anagrafica;
import it.arsinfo.smd.entity.Campagna;
import it.arsinfo.smd.entity.EstrattoConto;
import it.arsinfo.smd.entity.Pubblicazione;
import it.arsinfo.smd.repository.AbbonamentoDao;
import it.arsinfo.smd.repository.AnagraficaDao;
import it.arsinfo.smd.repository.CampagnaDao;
import it.arsinfo.smd.repository.EstrattoContoDao;
import it.arsinfo.smd.repository.PubblicazioneDao;

@SpringUI(path = SmdUI.URL_ABBONAMENTI)
@Title("Abbonamenti ADP")
public class AbbonamentoUI extends SmdUI {

    /**
     * 
     */
    private static final long serialVersionUID = 3429323584726379968L;

    private static final Logger log = LoggerFactory.getLogger(AbbonamentoUI.class);

    @Autowired
    AbbonamentoDao abbonamentoDao;

    @Autowired
    EstrattoContoDao estrattoContoDao;

    @Autowired
    AnagraficaDao anagraficaDao;

    @Autowired
    PubblicazioneDao pubblicazioneDao;

    @Autowired
    CampagnaDao campagnaDao;

    @Autowired
    SmdService smdService;

    @Override
    protected void init(VaadinRequest request) {
        super.init(request, "Abbonamento");

        
        List<Anagrafica> anagrafica = anagraficaDao.findAll();
        List<Pubblicazione> pubblicazioni = pubblicazioneDao.findAll();
        List<Campagna> campagne = campagnaDao.findAll();
        AbbonamentoAdd add = new AbbonamentoAdd("Aggiungi abbonamento");
        if (anagrafica.size() == 0) {
            add.setVisible(false);
        } else {
            add.setPrimoIntestatario(anagrafica.iterator().next());
        }
        EstrattoContoGrid estrattoContoGrid = new EstrattoContoGrid("Estratti Conto");
        AbbonamentoSearch search = new AbbonamentoSearch(abbonamentoDao,anagrafica,campagne);
        AbbonamentoGrid grid = new AbbonamentoGrid("Abbonamenti");
        AbbonamentoEditor editor = new AbbonamentoEditor(abbonamentoDao,anagrafica,campagne) {
            @Override
            public void delete() {
                if (get().getId() == null) {
                    Notification.show("Abbonamento non Salvato", Notification.Type.ERROR_MESSAGE);
                    return;
                }
                if (get().getCampagna() != null) {
                    Notification.show("Abbonamento associato a Campagna va gestito da Storico", Notification.Type.ERROR_MESSAGE);
                    return;
                }
                try {
                    smdService.deleteAbbonamento(get());
                } catch (Exception e) {
                    log.warn("save failed for :" + get().toString() +". Error log: " + e.getMessage());
                    Notification.show("Abbonamento non eliminato:" + e.getMessage(), Type.ERROR_MESSAGE);
                    return;                    
                }
                onChange();

            }
            
            @Override
            public void save() {                
                if (get().getId() == null && get().getAnno() == null) {
                    Notification.show("Selezionare Anno Prima di Salvare", Notification.Type.ERROR_MESSAGE);
                    return;
                }
                if (get().getId() == null && get().getAnno().getAnno() < Anno.getAnnoCorrente().getAnno()) {
                    Notification.show("Anno deve essere anno corrente o successivi", Notification.Type.ERROR_MESSAGE);
                    return;
                }
                if (get().getId() == null && getEstrattiConto().size() == 0) {
                    Notification.show("Aggiungere Estratto Conto Prima di Salvare", Notification.Type.ERROR_MESSAGE);
                    return;
                }
                if (get().getId() == null) {
                    get().setCampo(Abbonamento.generaCodeLine(get().getAnno()));
                }
                if (get().getId() != null && get().getStatoAbbonamento() == StatoAbbonamento.Annullato) {
                    try {
                        smdService.annullaAbbonamento(get());
                        onChange();
                    } catch (Exception e) {
                        log.warn("save failed for :" + get().toString() +". Error log: " + e.getMessage());
                        Notification.show("Non è possibile annullare abbonamento:" +e.getMessage(),
                                          Notification.Type.ERROR_MESSAGE);
                        return;
                    }
                }
                if (get().getId() != null ) {
                    super.save();
                    return;
                }
                try {
                    smdService.generaAbbonamento(get(), getEstrattiConto());
                } catch (Exception e) {
                    log.warn("save failed for :" + get().toString() +". Error log: " + e.getMessage());
                    Notification.show("Non è possibile salvare questo recordo è utilizzato da altri elementi.",
                                      Notification.Type.ERROR_MESSAGE);
                }
                onChange();
            }
        };

        EstrattoContoAdd estrattoContoAdd = new EstrattoContoAdd("Aggiungi EC");
        EstrattoContoEditor estrattoContoEditor = new EstrattoContoEditor(estrattoContoDao, pubblicazioni, anagrafica) {
            @Override
            public void save() {
                if (get().getDestinatario() == null) {
                    Notification.show("Selezionare il Destinatario",Notification.Type.WARNING_MESSAGE);
                    return;
                }
                if (get().getPubblicazione() == null) {
                    Notification.show("Selezionare la Pubblicazione",Notification.Type.WARNING_MESSAGE);
                    return;
                }
                if (get().getId() == null ) {
                    get().setAbbonamento(editor.get());
                    editor.addEstrattoConto(get());
                    onChange();
                    return;
                }
                try {
                    smdService.aggiornaECAbbonamento(editor.get(), get());
                } catch (Exception e) {
                    Notification.show(e.getMessage(),Notification.Type.ERROR_MESSAGE);
                    return;
                }
                onChange();
            };
            
            @Override 
            public void delete() {
                if (get().getId() == null ) {
                    editor.remove(get());
                    onChange();
                    return;
                }
                try {
                    smdService.cancellaECAbbonamento(editor.get(), get());
                } catch (Exception e) {
                    Notification.show(e.getMessage(),Notification.Type.WARNING_MESSAGE);
                    return;
                }
                onChange();
                
            };
        };

        addSmdComponents(estrattoContoEditor,editor,estrattoContoAdd,estrattoContoGrid, add,search, grid);

        editor.setVisible(false);
        estrattoContoEditor.setVisible(false);
        estrattoContoAdd.setVisible(false);
        estrattoContoGrid.setVisible(false);
        
        add.setChangeHandler(() -> {
            setHeader("Abbonamento:Nuovo");
            hideMenu();
            add.setVisible(false);
            search.setVisible(false);
            grid.setVisible(false);
            editor.edit(add.generate());
            estrattoContoAdd.setAbbonamento(editor.get());
            estrattoContoAdd.setVisible(true);
        });
        
        search.setChangeHandler(() -> grid.populate(search.find()));

        grid.setChangeHandler(() -> {
            if (grid.getSelected() == null) {
                return;
            }
            setHeader(grid.getSelected().getHeader());
            hideMenu();
            add.setVisible(false);
            search.setVisible(false);
            editor.edit(grid.getSelected());
            estrattoContoAdd.setVisible(false);
            estrattoContoEditor.setVisible(false);
            estrattoContoGrid.populate(
               estrattoContoDao.findByAbbonamento(
              grid.getSelected())
               .stream().map(ec -> {
                   ec.setAbbonamento(grid.getSelected());
                   return ec;
               }).collect(Collectors.toList())
               
               );
        });

        editor.setChangeHandler(() -> {
            editor.setVisible(false);
            estrattoContoAdd.setVisible(false);
            estrattoContoEditor.setVisible(false);
            estrattoContoGrid.setVisible(false);
            setHeader("Abbonamento");
            showMenu();
            add.setVisible(true);
            search.setVisible(true);
            grid.populate(search.find());
        });
        
        estrattoContoAdd.setChangeHandler(() -> {
            setHeader(String.format("%s:Spedizione:Nuova",editor.get().getHeader()));
            hideMenu();
            estrattoContoEditor.edit(estrattoContoAdd.generate());
            editor.setVisible(false);
            estrattoContoAdd.setVisible(false);
        });
        
        estrattoContoEditor.setChangeHandler(() -> {
            setHeader("Abbonamento:Nuovo");
            estrattoContoAdd.setVisible(editor.get().getId() == null);
            estrattoContoEditor.setVisible(false);
            editor.edit(estrattoContoEditor.get().getAbbonamento());
            estrattoContoGrid.populate(editor.getEstrattiConto());
        });
        
        estrattoContoGrid.setChangeHandler(() -> {
            if (estrattoContoGrid.getSelected() == null) {
                return;
            }
            if (editor.get().getId() == null) {
                setHeader(estrattoContoGrid.getSelected().getHeader());
                estrattoContoEditor.edit(estrattoContoGrid.getSelected());
                add.setVisible(false);
                search.setVisible(false);
                editor.setVisible(false);
                estrattoContoAdd.setVisible(false);
            }
        });

        grid.populate(search.findAll());

    }

    public List<EstrattoConto> findByAbbonamaneto(Abbonamento abbonamento) {
        return estrattoContoDao.findByAbbonamento(abbonamento);
    }
}
