package lt.linas_puplauskas;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import lt.linas_puplauskas.model.Comment;
import lt.linas_puplauskas.model.Ticket;
import lt.linas_puplauskas.model.dto.AIAnalysisResult;
import lt.linas_puplauskas.repository.CommentRepository;
import lt.linas_puplauskas.repository.TicketRepository;
import lt.linas_puplauskas.service.GroqService;

@Route("")
public class MainView extends VerticalLayout {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final GroqService groqService;

    private Grid<Ticket> ticketGrid;

    public MainView(CommentRepository commentRepository,
                    TicketRepository ticketRepository,
                    GroqService groqService) {
        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
        this.groqService = groqService;

        setPadding(true);
        setSpacing(true);

        add(buildHeader());
        add(buildCommentForm());
        add(buildTicketGrid());

        refreshTickets();
    }

    private H1 buildHeader() {
        return new H1("PulseDesk");
    }

    private VerticalLayout buildCommentForm() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);

        H2 title = new H2("Submit a Comment");

        TextArea textArea = new TextArea("Your comment");
        textArea.setPlaceholder("Describe your issue or feedback...");
        textArea.setWidthFull();
        textArea.setMinHeight("120px");

        Button submit = new Button("Submit", event -> {
            String content = textArea.getValue().trim();
            if (content.isEmpty()) {
                Notification.show("Please enter a comment.");
                return;
            }

            Comment comment = new Comment();
            comment.setContent(content);
            commentRepository.save(comment);

            AIAnalysisResult result = groqService.analyze(content);

            if (result.isShouldBeTicket()) {
                Ticket ticket = new Ticket();
                ticket.setTitle(result.getTitle());
                ticket.setCategory(result.getCategory());
                ticket.setPriority(result.getPriority());
                ticket.setSummary(result.getSummary());
                ticket.setComment(comment);
                ticketRepository.save(ticket);
                Notification.show("Comment submitted and ticket created!");
            } else {
                Notification.show("Comment submitted — no ticket needed.");
            }

            textArea.clear();
            refreshTickets();
        });

        layout.add(title, textArea, submit);
        return layout;
    }

    private VerticalLayout buildTicketGrid() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);

        H2 title = new H2("Tickets");

        ticketGrid = new Grid<>(Ticket.class, false);
        ticketGrid.addColumn(Ticket::getTitle).setHeader("Title").setAutoWidth(true);
        ticketGrid.addColumn(Ticket::getCategory).setHeader("Category").setAutoWidth(true);
        ticketGrid.addColumn(Ticket::getPriority).setHeader("Priority").setAutoWidth(true);
        ticketGrid.addColumn(Ticket::getSummary).setHeader("Summary").setAutoWidth(true);
        ticketGrid.addColumn(Ticket::getCreatedAt).setHeader("Created At").setAutoWidth(true);
        ticketGrid.setWidthFull();

        layout.add(title, ticketGrid);
        return layout;
    }

    private void refreshTickets() {
        ticketGrid.setItems(ticketRepository.findAll());
    }
}