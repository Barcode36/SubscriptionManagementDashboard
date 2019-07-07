package it.arsinfo.smd.ui.vaadin;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;

import it.arsinfo.smd.data.Anno;
import it.arsinfo.smd.data.Cassa;
import it.arsinfo.smd.data.Ccp;
import it.arsinfo.smd.data.StatoAbbonamento;
import it.arsinfo.smd.entity.Abbonamento;
import it.arsinfo.smd.entity.Anagrafica;
import it.arsinfo.smd.entity.Campagna;
import it.arsinfo.smd.repository.AbbonamentoDao;

public class AbbonamentoSearch extends SmdSearch<Abbonamento> {

    private String searchCampo;
    private Anagrafica customer;
    private Anno anno;
    private Campagna campagna;
    private final ComboBox<Ccp> filterCcp = new ComboBox<Ccp>();
    private final ComboBox<Cassa> filterCassa = new ComboBox<Cassa>();
    private final ComboBox<StatoAbbonamento> filterStatoAbbonamento
    = new ComboBox<StatoAbbonamento>();
    public AbbonamentoSearch(AbbonamentoDao abbonamentoDao,
            List<Anagrafica> anagrafica, List<Campagna> campagne) {
        super(abbonamentoDao);

        ComboBox<Anagrafica> filterAnagrafica = new ComboBox<Anagrafica>();
        ComboBox<Anno> filterAnno = new ComboBox<Anno>();
        ComboBox<Campagna> filterCampagna = new ComboBox<Campagna>();
        
        TextField filterCampo = new TextField();


        setComponents(new HorizontalLayout(filterAnagrafica,filterStatoAbbonamento,filterAnno),
                      new HorizontalLayout(filterCampo,filterCampagna,filterCassa,filterCcp));

        filterCampo.setPlaceholder("Cerca per V Campo");
        filterCampo.setValueChangeMode(ValueChangeMode.EAGER);
        filterCampo.addValueChangeListener(e -> {
            searchCampo = e.getValue();
            onChange();
        });

        filterCampagna.setEmptySelectionAllowed(true);
        filterCampagna.setPlaceholder("Cerca per Campagna");
        filterCampagna.setItems(campagne);
        filterCampagna.setItemCaptionGenerator(Campagna::getCaption);
        filterCampagna.addSelectionListener(e -> {
            if (e.getValue() == null) {
                campagna = null;
            } else {
                campagna=e.getSelectedItem().get();
            }
            onChange();
        });

        filterAnno.setEmptySelectionAllowed(true);
        filterAnno.setPlaceholder("Cerca per Anno");
        filterAnno.setItems(EnumSet.allOf(Anno.class));
        filterAnno.setItemCaptionGenerator(Anno::getAnnoAsString);
        filterAnno.addSelectionListener(e -> {
            if (e.getValue() == null) {
                anno = null;
            } else {
                anno=e.getSelectedItem().get();
            }
            onChange();
        });

        filterAnagrafica.setEmptySelectionAllowed(true);
        filterAnagrafica.setPlaceholder("Cerca per Intestatario");
        filterAnagrafica.setItems(anagrafica);
        filterAnagrafica.setItemCaptionGenerator(Anagrafica::getCaption);
        filterAnagrafica.addSelectionListener(e -> {
            if (e.getValue() == null) {
                customer = null;
            } else {
                customer = e.getSelectedItem().get();
            }
            onChange();
        });
        
        filterCassa.setPlaceholder("Cerca per Cassa");
        filterCassa.setItems(EnumSet.allOf(Cassa.class));
        filterCassa.addSelectionListener(e ->onChange());

        filterStatoAbbonamento.setPlaceholder("Cerca per Stato");
        filterStatoAbbonamento.setItems(EnumSet.allOf(StatoAbbonamento.class));
        filterStatoAbbonamento.addSelectionListener(e ->onChange());

        filterCcp.setPlaceholder("Cerca per Cc");
        filterCcp.setItems(EnumSet.allOf(Ccp.class));
        filterCcp.setItemCaptionGenerator(Ccp::getCcp);
        filterCcp.addSelectionListener(e ->onChange());


    }

    @Override
    public List<Abbonamento> find() {
        if (campagna == null && customer == null && anno == null) {
            return filterAll(findAll());            
        }
        if (campagna == null && anno == null) {
            return filterAll(((AbbonamentoDao) getRepo()).findByIntestatario(customer));
        }
        if (customer == null && anno == null) {
            return filterAll(((AbbonamentoDao) getRepo()).findByCampagna(campagna));
        }
        if (customer == null && campagna == null) {
            return filterAll(((AbbonamentoDao) getRepo()).findByAnno(anno));
        }
        
        if (anno == null) {
           return filterAll(((AbbonamentoDao) getRepo()).findByIntestatario(customer)
            .stream()
            .filter(a -> 
                a.getCampagna() != null
                && a.getCampagna().getId() == campagna.getId()
            )
            .collect(Collectors.toList()));
        }
        if (campagna == null) {
           return filterAll(((AbbonamentoDao) getRepo()).findByIntestatario(customer)
            .stream()
            .filter(a -> 
                a.getAnno() == anno
                )
            .collect(Collectors.toList()));
        }
        if (customer == null) {
            return filterAll(((AbbonamentoDao) getRepo()).findByCampagna(campagna)
                .stream()
                    .filter(a -> 
                        a.getAnno() == anno
                        )
                    .collect(Collectors.toList()));
        }
        return filterAll(((AbbonamentoDao) getRepo()).findByIntestatario(customer)
                .stream()
                .filter(a -> 
                    a.getCampagna() != null
                    && a.getCampagna().getId() == campagna.getId()
                    && a.getAnno() == anno
                    )
                .collect(Collectors.toList()));
    }

    private List<Abbonamento> filterAll(List<Abbonamento> abbonamenti) {
        if (filterCcp.getValue() != null) {
            abbonamenti=abbonamenti.stream().filter(a -> a.getCcp() == filterCcp.getValue()).collect(Collectors.toList());      
        }
        if (filterCassa.getValue() != null) {
            abbonamenti=abbonamenti.stream().filter(a -> a.getCassa() == filterCassa.getValue()).collect(Collectors.toList());      
        }
        if (filterStatoAbbonamento.getValue() != null) {
            abbonamenti=abbonamenti.stream().filter(a -> a.getStatoAbbonamento() == filterStatoAbbonamento.getValue()).collect(Collectors.toList());      
        }
        if (!StringUtils.isEmpty(searchCampo)) {
            abbonamenti=abbonamenti.stream().filter(a -> a.getCampo().toLowerCase().contains(searchCampo.toLowerCase())).collect(Collectors.toList());                  
        }
        return abbonamenti;
    }

}
