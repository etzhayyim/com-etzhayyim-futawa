(ns futawa.murakumo
  "Pure cljc actor boundary generated from manifest migration scaffold."
  (:require [clojure.string :as str]))

(def actor-did
  "did:web:etzhayyim.com:futawa")

(def common-gates
  [:council-charter-attestation
   :no-platform-held-key-baseline
   :no-probing-baseline
   :murakumo-only-inference-baseline
   :did-primary-baseline
   :append-only-gate-baseline
   :kotoba-only-substrate-baseline])

(defn collection
  [name]
  (str "com.etzhayyim.futawa." name))

(def cell-specs {
  :moto_frame_welding {:legacy-cell "moto-frame-welding"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "moto_frame_welding")]
     :required-gates common-gates
     :trigger "manifest cell moto_frame_welding"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :moto_engine_assembly {:legacy-cell "moto-engine-assembly"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "moto_engine_assembly")]
     :required-gates common-gates
     :trigger "manifest cell moto_engine_assembly"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :moto_drivetrain_assembly {:legacy-cell "moto-drivetrain-assembly"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "moto_drivetrain_assembly")]
     :required-gates common-gates
     :trigger "manifest cell moto_drivetrain_assembly"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :moto_electrical_harness {:legacy-cell "moto-electrical-harness"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "moto_electrical_harness")]
     :required-gates common-gates
     :trigger "manifest cell moto_electrical_harness"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :moto_suspension_brake {:legacy-cell "moto-suspension-brake"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "moto_suspension_brake")]
     :required-gates common-gates
     :trigger "manifest cell moto_suspension_brake"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :moto_body_paint {:legacy-cell "moto-body-paint"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "moto_body_paint")]
     :required-gates common-gates
     :trigger "manifest cell moto_body_paint"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :moto_final_assembly {:legacy-cell "moto-final-assembly"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "moto_final_assembly")]
     :required-gates common-gates
     :trigger "manifest cell moto_final_assembly"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :moto_test_dyno_road {:legacy-cell "moto-test-dyno-road"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "moto_test_dyno_road")]
     :required-gates common-gates
     :trigger "manifest cell moto_test_dyno_road"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :moto_provenance_binder {:legacy-cell "moto-provenance-binder"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "moto_provenance_binder")]
     :required-gates common-gates
     :trigger "manifest cell moto_provenance_binder"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
})

(defn safe-rkey
  [s]
  (let [clean (-> (str s)
                  (str/replace #"^did:web:" "")
                  (str/replace #"[^A-Za-z0-9._~-]" "-"))]
    (if (str/blank? clean) "unknown" clean)))

(defn gate-value
  [attestations gate]
  (or (get attestations gate)
      (get attestations (name gate))
      (when (set? attestations) (attestations gate))
      (when (set? attestations) (attestations (name gate)))))

(defn missing-gates
  [spec attestations]
  (->> (:required-gates spec)
       (remove #(boolean (gate-value attestations %)))
       vec))

(defn put-record-effect
  [collection rkey record]
  {:op :mst/put-record
   :actor actor-did
   :collection collection
   :rkey rkey
   :record record})

(defn records-for
  [spec {:keys [records record computed-at request-id]
         :as input}]
  (let [input-records (cond
                        (map? records) records
                        (some? record) {0 record}
                        :else {})
        base {:actorDid actor-did
              :computedAt computed-at
              :legacyCell (:legacy-cell spec)
              :phase (:phase spec)
              :requestId request-id
              :actorBoundary "cljc-migration-scaffold"
              :scaffold true
              :constitutionalStatus "attested-plan"}]
    (map-indexed
     (fn [idx coll]
       (let [record* (merge {:$type coll}
                            base
                            (or (get input-records coll)
                                (get input-records idx)
                                {}))
             rkey (safe-rkey (or (:rkey record*)
                                 (get record* "rkey")
                                 (:tid record*)
                                 request-id
                                 (str (:legacy-cell spec) "-" idx)))]
         {:collection coll
          :record record*
          :rkey rkey}))
     (:collections spec))))

(defn cell-plan
  [cell-key {:keys [attestations] :as input}]
  (let [spec (get cell-specs cell-key)]
    (when-not spec
      (throw (ex-info "unknown cell" {:cell cell-key})))
    (let [missing (missing-gates spec attestations)]
      (merge
       {:cell cell-key
        :legacy-cell (:legacy-cell spec)
        :actor actor-did
        :phase (:phase spec)
        :murakumo-node (:murakumo-node spec)
        :trigger (:trigger spec)
        :ceiling (:ceiling spec)
        :required-gates (:required-gates spec)
        :missing-gates missing}
       (if (seq missing)
         {:status :blocked
          :effects []}
         (let [planned-records (records-for spec input)]
           {:status :ready
            :records (vec planned-records)
            :effects (mapv (fn [{:keys [collection record rkey]}]
                             (put-record-effect collection rkey record))
                           planned-records)}))))))

(defn all-cell-plans
  [input]
  (into {}
        (map (fn [cell-key] [cell-key (cell-plan cell-key input)]))
        (keys cell-specs)))
