(define (caar x) (car (car x)))
(define (cadr x) (car (cdr x)))
(define (cddr x) (cdr (cdr x)))
(define (cadar x) (car (cdr (car x))))

; Some utility functions that you may find useful to implement.
(define (map proc items)
  (if (null? items)
      nil
      (cons (proc (car items))
            (map proc (cdr items)))))

(define (cons-all first rests)
  (if (null? rests)
    '()
    (cons (append (list first) (car rests))
          (cons-all first (cdr rests)))
    )
  )
(define (zip2 pairs)
  (define (first ls)
    (if (null? ls) '()
      (cons (caar ls) (first (cdr ls)))
      )
    )
  (define (second ls)
    (if (null? ls) '()
      (cons (cadar ls) (second (cdr ls)))
      )
    )
  (list (first pairs) (second pairs))
  )

(define (zip l1 l2)
  (if (or (null? l1) (null? l2))
      '()
      (cons (list (car l1) (car l2))
            (zip  (cdr l1) (cdr l2)))))

;; Problem 18
;; Returns a list of two-element lists
(define (enumerate s)
  ; BEGIN Question 18
  (define (ranger r s)
    (if (>= r s)
      '()
      (append (list r) (ranger (+ r 1) s))
      ) 
    )
  (define ls (ranger 0 (length s)))
  (zip ls s)
  )
  ; END Question 18

;; Problem 19
;; List all ways to make change for TOTAL with DENOMS
(define (list-change total denoms)
  ; BEGIN Question 19
  (cond ((or (null? denoms) (= total 0)) '())
    ((> (car denoms) total) (list-change total (cdr denoms)))
    ((= (car denoms) total) (cons (list (car denoms)) (list-change total (cdr denoms))))
    (else (append (cons-all (car denoms) (list-change (- total (car denoms)) denoms)) (list-change total (cdr denoms))))
    )
  )
  ; END Question 19

;; Problem 20
;; Returns a function that checks if an expression is the special form FORM
(define (check-special form)
  (lambda (expr) (equal? form (car expr))))

(define lambda? (check-special 'lambda))
(define define? (check-special 'define))
(define quoted? (check-special 'quote))
(define let?    (check-special 'let))

;; Converts all let special forms in EXPR into equivalent forms using lambda
(define (analyze expr)
  (cond ((atom? expr)
     ; BEGIN Question 20
     expr
     ; END Question 20
     )
    ((quoted? expr)
     ; BEGIN Question 20
      expr
     ; END Question 20
     )
    ((or (lambda? expr)
         (define? expr))
     (let ((form   (car expr))
           (params (cadr expr))
           (body   (cddr expr)))
       ; BEGIN Question 20
       (cons form (cons params (analyze body)))
       ; END Question 20
       ))
    ((let? expr)
     (let ((values (cadr expr))
           (body   (cddr expr)))
       ; BEGIN Question 20
        (define bindings (zip2 values))
        (cons (cons 'lambda (cons (car bindings) (analyze body))) (analyze (cadr bindings)))
       ; END Question 20
       ))
    (else
     ; BEGIN Question 20
     (map analyze expr)
     ; END Question 20
     )))

;; Problem 21 (optional)
;; Draw the hax image using turtle graphics.
(define (hax d k)
  ; BEGIN Question 21
  'REPLACE-THIS-LINE
  )
  ; END Question 21

