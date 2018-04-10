; тест комментария
(define ego (person 'Евгений' 'Грязнов'))



(define void? (lambda (obj)
                (of-type? obj 'void')
              )
)



; Инкремент
(define inc (lambda (n)
               (+ n 1)
             )
)



; Декремент
(define dec (lambda (n)
              (- n 1)
            )
)



(define first-n (lambda (n)
                  (if (= n 0)
                    (list 0)
                    (append (first-n (dec n)) n)
                  )
                )
)



(define odd? (lambda (n) (= 1 (mod n 2))))



(define male? (lambda (p) (= 'male' (attr p 'sex'))))



(define female? (lambda (p) (not (male? p))))



(define underage? (lambda (p) (< 18 (attr p 'age'))))



(define iter (lambda (match? default combiner transform current next finish result)
               ( if (= current finish)
                    (combiner result (transform finish))
                    (iter match? default combiner transform (next current) next finish (if (match? current)
                                                                                           (combiner result (transform current))
                                                                                           result
                                                                                       )
                    )
               )
             )
)



(define filtered-accumulate (lambda (match? default combiner transform current next finish)
                                      (iter match? default combiner transform current next finish default)
                              )
)



(define truth (lambda (o) true))



(define id (lambda (x) x))



(define factorial (lambda (n) (filtered-accumulate truth 1 * id 1 inc n)))



( define twice (lambda (f) ( lambda (n) (f (f n)) )) )



( define square (lambda (n) (* n n)) )



(define two 2)



(define test (lambda () two))
