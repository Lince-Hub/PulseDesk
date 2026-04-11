package lt.linas_puplauskas.controller;

import lombok.RequiredArgsConstructor;
import lt.linas_puplauskas.model.Comment;
import lt.linas_puplauskas.model.Ticket;
import lt.linas_puplauskas.model.dto.AIAnalysisResult;
import lt.linas_puplauskas.repository.CommentRepository;
import lt.linas_puplauskas.repository.TicketRepository;
import lt.linas_puplauskas.service.GroqService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final GroqService groqService;

    @GetMapping
    public List<Comment> getAll() {
        return commentRepository.findAll();
    }

    @PostMapping
    public Comment create(@RequestBody String content) {
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
        }

        return commentRepository.findById(comment.getId()).orElseThrow();
    }
}