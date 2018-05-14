; тест комментария
(define ego (person 'Евгений' 'Грязнов'))

(define square (lambda (n) (* n n)))

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

(define age (lambda (p) (attr p 'age')))

(define odd? (lambda (n) (= 1 (mod n 2))))

(define male? (lambda (p) (= 'male' (attr p 'sex'))))

(define female? (lambda (p) (not (male? p))))

(define underage? (lambda (p) (> 18 (age p))))

(define iter (lambda (match? combiner transform current next finish result)
               ( if (= current finish)
                    (combiner result (transform finish))
                    (iter match? combiner transform (next current) next finish (if (match? current)
                                                                                    (combiner result (transform current))
                                                                                    result
                                                                                )
                    )
               )
             )
)

(define filtered-accumulate (lambda (match? combiner transform start next finish)
                                      (iter match? combiner transform start next finish start)
                              )
)

(define truth (lambda (o) true))

(define id (lambda (x) x))

(define invert (lambda (bool) (lambda (x) (not (bool x))) ))

(define compose (lambda (g f) (lambda (x) (g (f x)))))

(define twice (lambda (f) ( lambda (n) (f (f n)) )))

(define factorial (lambda (n) (if (= n 0) 1 (* n (factorial (dec n))))))

(define empty? (lambda (lst) (= 0 (count lst))))

(define find-first-rec (lambda (item lst index)
                            (if (empty? lst)
                                void
                                (if (= item (head lst))
                                    index
                                    (find-first-rec item (tail lst) (inc index))
                                )
                            )
                       )
)

(define find-first (lambda (item lst) (find-first-rec item lst 0)))

(define relative? (lambda (p1 p2)
                          (and
                            (= void (find-first 'husband' (kinship p1 p2)))
                            (= void (find-first 'wife'    (kinship p1 p2)))
                          )
                  )
)

(define relatives (lambda (person) (filter (lambda (p) (relative? person p)) people)))

(define related? (lambda (p1 p2) (= vacant (kinship p1 p2))))

(define not-related? (lambda (p1 p2) (not (related? p1 p2))))

(define same-gen (lambda (person)
                   (filter
                     (lambda (p)
                       (and (= 0 (gen-dist person p))
                            (not-related? person p)
                       )
                     )
                     people
                   )
                 )
)

(define females (filter female? people))

(define males (filter male? people))

(define in-law? (lambda (p1 p2) (not (relative? p1 p2))))

(define in-laws (lambda (person) (filter (lambda (p) (in-law? person p)) people)))

(define kids (filter underage? people))

(define adults (filter (invert underage?) people))

(define fibonacci
    (lambda (n)
        (if (< n 2)
            1
            (+ (fibonacci (- n 1)) (fibonacci (- n 2)))
        )
    )
)

(define parents (lambda (p) (join (mother p) (father p))))

(define cousins (lambda (p) (children (children (parents (parents p))))))

(define WWII-start (date '01.09.1939'))

(define WWII-end (date '02.09.1945'))

(define times-n (lambda (f n) (lambda (x) (if (= n 0)
                                              x
                                              ((times-n f (- n 1)) (f x))
                                          )
                              )
                )
)

(define double (lambda (n) (* 2 n)))

(define apply (lambda (f x) (f x)))
